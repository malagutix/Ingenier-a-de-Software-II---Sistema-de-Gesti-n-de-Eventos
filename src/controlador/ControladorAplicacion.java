package controlador;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import modelo.DatosDePrueba;
import modelo.Modelo;
import modelo.persistencia.BaseDeDatos;
import modelo.persistencia.ConfiguracionBaseDatos;
import modelo.persistencia.ErrorBaseDatosException;
import modelo.usuarios.servicio.BandejaCorreoSimulada;
import modelo.usuarios.servicio.ConfiguracionCorreo;
import modelo.usuarios.servicio.ServicioCorreo;
import modelo.usuarios.servicio.ServicioCorreoSMTP;
import vista.VistaArranque.OpcionConexion;
import vista.VistaArranque;
import vista.VistaPrincipal;

/**
 * Arranque de la aplicación: lee la configuración de correo y de base de
 * datos, se conecta a PostgreSQL (CDU 007: crea la base y las tablas si no
 * existen), arma el modelo y abre la ventana principal con su controlador.
 *
 * Si no hay basedatos.properties (o tiene habilitado=false) el sistema
 * funciona en modo demostración, con los datos en memoria.
 */
public class ControladorAplicacion extends Controlador {

    private static final int UNA_HORA_MS = 60 * 60 * 1000;

    private final VistaArranque vista = new VistaArranque();
    /** Por qué se trabaja en memoria (se muestra en el login y en la pestaña Base de datos), o null. */
    private String motivoSinBase;
    /** El mismo motivo, en una línea corta para el login. */
    private String motivoCorto;

    public void iniciar() {
        VistaPrincipal.configurarAspecto();
        instalarManejadorDeErrores();

        // Correo: real si existe correo.properties con habilitado=true; si no, bandeja simulada
        BandejaCorreoSimulada bandeja = null;
        ServicioCorreo correo = null;
        try {
            ConfiguracionCorreo config = ConfiguracionCorreo.cargar(buscarArchivo("correo.properties"));
            correo = config != null ? new ServicioCorreoSMTP(config) : null;
        } catch (IOException | RuntimeException e) {
            vista.advertir("Correo", "No se pudo leer correo.properties: " + e.getMessage()
                    + "\nSe usará la bandeja de correo simulada.");
        }
        if (correo == null) {
            bandeja = new BandejaCorreoSimulada();
            correo = bandeja;
        }

        BaseDeDatos bd = conectar();
        Modelo modelo;
        String usuariosDePrueba;
        String aviso;
        if (bd != null) {
            modelo = Modelo.conPostgres(bd, correo);
            usuariosDePrueba = modelo.getGestorBaseDatos().tieneDatosDeEjemplo()
                    ? DatosDePrueba.RESUMEN_SIN_ADMIN : null;
            ConfiguracionBaseDatos c = bd.getConfiguracion();
            aviso = "Datos guardados en PostgreSQL: base " + c.getBase() + " en " + c.getServidor() + ":"
                    + c.getPuerto();
            programarRespaldos(modelo);
            Runtime.getRuntime().addShutdownHook(new Thread(bd::cerrar));
        } else {
            modelo = Modelo.enMemoria(correo);
            DatosDePrueba.cargar(modelo.getGestor(), modelo.getCatalogos(), modelo.getSistema());
            usuariosDePrueba = DatosDePrueba.RESUMEN;
            aviso = "<html>Modo demostración: los datos están en memoria y se pierden al cerrar.<br>"
                    + "Motivo: " + motivoCorto + " Más detalles en la pestaña Base de datos.</html>";
        }

        VistaPrincipal ventana = new VistaPrincipal();
        new ControladorPrincipal(ventana, modelo, bandeja, usuariosDePrueba, aviso, motivoSinBase);
        ventana.setVisible(true);
    }

    /**
     * Se conecta a PostgreSQL según basedatos.properties. Devuelve null si hay
     * que trabajar en memoria (no hay configuración o el usuario lo eligió).
     */
    private BaseDeDatos conectar() {
        Path archivo = buscarArchivo("basedatos.properties");
        if (!Files.exists(archivo)) {
            motivoSinBase = "no se encontró basedatos.properties (se buscó en " + lugaresDeBusqueda() + ").";
            motivoCorto = "no se encontró basedatos.properties.";
            return null;
        }
        ConfiguracionBaseDatos config;
        try {
            config = ConfiguracionBaseDatos.cargar(archivo);
        } catch (IOException e) {
            motivoSinBase = "basedatos.properties tiene un error: " + e.getMessage();
            motivoCorto = "basedatos.properties tiene un error.";
            decidir("El archivo basedatos.properties tiene un error:\n" + e.getMessage(), false);
            return null;
        }
        if (config == null) {
            motivoSinBase = "basedatos.properties tiene habilitado=false (" + archivo + ").";
            motivoCorto = "basedatos.properties tiene habilitado=false.";
            return null;
        }
        while (true) {
            BaseDeDatos bd = new BaseDeDatos(config);
            try {
                bd.inicializar(); // CDU 007: verifica, crea la base si falta y genera las tablas
                return bd;
            } catch (ErrorBaseDatosException e) {
                bd.cerrar();
                if (!decidir(e.getMessage(), true)) {
                    motivoSinBase = "no se pudo conectar con PostgreSQL y se eligió el modo demostración ("
                            + e.getMessage() + ")";
                    motivoCorto = "no se pudo conectar con PostgreSQL.";
                    return null;
                }
            }
        }
    }

    /**
     * Busca un archivo de configuración en la carpeta desde la que se ejecuta
     * el programa y, si no está, en la carpeta del proyecto (subiendo desde
     * donde están las clases compiladas: out/ con ejecutar.bat, o
     * out/production/sistema-eventos/ con IntelliJ). Así funciona aunque
     * IntelliJ ejecute desde otra carpeta.
     *
     * @return el archivo encontrado, o la ubicación por defecto si no existe
     */
    static Path buscarArchivo(String nombre) {
        for (Path carpeta : carpetasDeBusqueda()) {
            Path candidato = carpeta.resolve(nombre);
            if (Files.exists(candidato)) {
                return candidato;
            }
        }
        return Paths.get(nombre).toAbsolutePath();
    }

    private static List<Path> carpetasDeBusqueda() {
        List<Path> carpetas = new ArrayList<>();
        carpetas.add(Paths.get("").toAbsolutePath());
        try {
            Path clases = Paths.get(ControladorAplicacion.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path carpeta = Files.isDirectory(clases) ? clases : clases.getParent();
            for (int i = 0; i < 4 && carpeta != null; i++) {
                if (!carpetas.contains(carpeta)) {
                    carpetas.add(carpeta);
                }
                carpeta = carpeta.getParent();
            }
        } catch (URISyntaxException | RuntimeException e) {
            // solo se busca en la carpeta actual
        }
        // Si IntelliJ abrió la carpeta que contiene a sistema-eventos/ (por ejemplo
        // al descomprimir el zip), el archivo está en esa subcarpeta.
        for (Path carpeta : new ArrayList<>(carpetas)) {
            Path proyecto = carpeta.resolve("sistema-eventos");
            if (Files.isDirectory(proyecto) && !carpetas.contains(proyecto)) {
                carpetas.add(proyecto);
            }
        }
        return carpetas;
    }

    private static String lugaresDeBusqueda() {
        List<String> lugares = new ArrayList<>();
        for (Path p : carpetasDeBusqueda()) {
            lugares.add(p.toString());
        }
        return String.join(", ", lugares);
    }

    /** Devuelve true si hay que reintentar; sale de la aplicación si el usuario lo eligió. */
    private boolean decidir(String problema, boolean permiteReintentar) {
        OpcionConexion opcion = vista.preguntarSinConexion(problema, permiteReintentar);
        if (opcion == OpcionConexion.SALIR) {
            System.exit(0);
        }
        return opcion == OpcionConexion.REINTENTAR;
    }

    /** RNF 003: un respaldo automático por día (se revisa al iniciar y cada una hora). */
    private static void programarRespaldos(Modelo modelo) {
        Runnable respaldar = () -> {
            try {
                Path archivo = modelo.getGestorBaseDatos().respaldoAutomatico();
                if (archivo != null) {
                    System.out.println("Respaldo automático guardado en " + archivo);
                }
            } catch (ErrorBaseDatosException e) {
                System.err.println("No se pudo hacer el respaldo automático: " + e.getMessage());
            }
        };
        respaldar.run();
        Timer timer = new Timer(UNA_HORA_MS, e -> respaldar.run());
        timer.start();
    }

    /** Si algo falla fuera de una acción, se informa en pantalla en lugar de perderse en la consola. */
    private void instalarManejadorDeErrores() {
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> {
            if (!(error instanceof ErrorBaseDatosException)) {
                error.printStackTrace();
            }
            String mensaje = error instanceof ErrorBaseDatosException
                    ? error.getMessage()
                    : "Ocurrió un error inesperado: " + error;
            SwingUtilities.invokeLater(() -> vista.mostrarError(mensaje));
        });
    }
}
