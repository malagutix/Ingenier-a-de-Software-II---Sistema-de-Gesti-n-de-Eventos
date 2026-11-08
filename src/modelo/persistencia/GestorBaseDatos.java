package modelo.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Intervalo;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.repositorio.LocacionRepository;
import modelo.eventos.servicio.ReglasPlanificacion.Ocupacion;
import modelo.eventos.servicio.ReglasPlanificacion;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.excepciones.AccesoDenegadoException;
import modelo.usuarios.servicio.GestorUsuarios;

/**
 * Patrón Facade para el CDU 007 - Gestionar base de datos. El administrador
 * consulta el estado de la base, las tablas y sus registros, hace respaldos,
 * los restaura y verifica la integridad. Las altas, bajas y modificaciones de
 * los datos se hacen desde las pantallas de cada módulo, que validan las
 * reglas del negocio antes de guardar.
 */
public class GestorBaseDatos {

    private static final int MAX_FILAS_CONSULTA = 1000;

    private final GestorUsuarios gestorUsuarios;
    private final BaseDeDatos bd;
    private final ContextoPersistencia ctx;
    private final Respaldo respaldo;
    private final ReglasPlanificacion reglas;
    private final LocacionRepository locaciones;

    public GestorBaseDatos(GestorUsuarios gestorUsuarios, BaseDeDatos bd, ContextoPersistencia ctx,
                           ReglasPlanificacion reglas, LocacionRepository locaciones) {
        this.gestorUsuarios = gestorUsuarios;
        this.bd = bd;
        this.ctx = ctx;
        this.respaldo = new Respaldo(ctx);
        this.reglas = reglas;
        this.locaciones = locaciones;
    }

    public EstadoBaseDatos obtenerEstado(Usuario actor) {
        verificar(actor);
        Map<String, Long> registros = new LinkedHashMap<>();
        LocalDateTime[] ultimo = new LocalDateTime[1];
        ctx.leer(c -> {
            for (String tabla : Esquema.TABLAS) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                    rs.next();
                    registros.put(tabla, rs.getLong(1));
                }
            }
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT ultimo_respaldo FROM control WHERE id = 1")) {
                if (rs.next()) {
                    ultimo[0] = rs.getObject(1, LocalDateTime.class);
                }
            }
            return null;
        });
        return new EstadoBaseDatos(bd.getConfiguracion(), bd.getInicio(), ultimo[0], carpetaRespaldos(), registros);
    }

    public List<String> listarTablas(Usuario actor) {
        verificar(actor);
        return Esquema.TABLAS;
    }

    /** ConsultaTabla los registros de una tabla (CDU 007, paso 5: consulta). Oculta datos sensibles. */
    public ConsultaTabla consultarTabla(Usuario actor, String tabla) {
        verificar(actor);
        if (!Esquema.TABLAS.contains(tabla)) {
            throw new ErrorBaseDatosException("Tabla desconocida: " + tabla);
        }
        return ctx.leer(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM " + tabla + " ORDER BY 1 LIMIT " + (MAX_FILAS_CONSULTA + 1));
                 ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                List<String> columnas = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    columnas.add(md.getColumnName(i));
                }
                List<Object[]> filas = new ArrayList<>();
                while (rs.next() && filas.size() < MAX_FILAS_CONSULTA) {
                    Object[] fila = new Object[columnas.size()];
                    for (int i = 0; i < fila.length; i++) {
                        String col = columnas.get(i);
                        String valor = rs.getString(i + 1);
                        boolean sensible = col.equals("contrasena_hash") || col.equals("codigo");
                        fila[i] = valor == null ? "" : sensible ? "••••••" : valor;
                    }
                    filas.add(fila);
                }
                boolean recortada = filas.size() == MAX_FILAS_CONSULTA && rs.next();
                return new ConsultaTabla(columnas, filas, recortada);
            }
        });
    }

    /** Respaldo manual en el archivo elegido (o en la carpeta de respaldos si es null). */
    public Path respaldar(Usuario actor, Path destino) {
        verificar(actor);
        Path archivo = destino != null ? destino : Respaldo.archivoNuevo(prepararCarpeta());
        respaldo.respaldar(archivo);
        return archivo;
    }

    /** Reemplaza todos los datos por los del respaldo. Después hay que volver a iniciar sesión. */
    public void restaurar(Usuario actor, Path archivo) {
        verificar(actor);
        try {
            respaldo.restaurar(archivo);
        } catch (ErrorBaseDatosException e) {
            if (e.getMessage().startsWith("No se pudo conectar")) {
                throw e;
            }
            String detalle = e.getMessage().startsWith("Error de base de datos")
                    ? "el archivo está dañado o incompleto" : e.getMessage();
            throw new ErrorBaseDatosException("No se pudo restaurar: " + detalle
                    + ". No se modificó ningún dato.", e);
        }
    }

    /**
     * Respaldo automático diario (RNF 003): si pasó más de un día desde el
     * último, guarda uno nuevo en la carpeta de respaldos y conserva los 7 más
     * recientes. Devuelve el archivo creado o null si no hacía falta.
     */
    public Path respaldoAutomatico() {
        LocalDateTime ultimo = ctx.leer(c -> {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT ultimo_respaldo FROM control WHERE id = 1")) {
                return rs.next() ? rs.getObject(1, LocalDateTime.class) : null;
            }
        });
        if (ultimo != null && ultimo.isAfter(LocalDateTime.now().minusDays(1))) {
            return null;
        }
        Path carpeta = prepararCarpeta();
        Path archivo = Respaldo.archivoNuevo(carpeta);
        respaldo.respaldar(archivo);
        Respaldo.depurar(carpeta);
        return archivo;
    }

    /**
     * Verifica la integridad (CDU 007, RF007 paso 5). Las claves foráneas y
     * restricciones las garantiza PostgreSQL; acá se revisan las reglas del
     * negocio que no se pueden expresar como restricción de tabla.
     */
    public List<String> verificarIntegridad(Usuario actor) {
        verificar(actor);
        List<String> informe = new ArrayList<>();
        int problemas = 0;
        int claves = ctx.leer(c -> {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.table_constraints"
                            + " WHERE table_schema = current_schema() AND constraint_type IN ('FOREIGN KEY', 'CHECK',"
                            + " 'UNIQUE', 'PRIMARY KEY')")) {
                rs.next();
                return rs.getInt(1);
            }
        });
        informe.add("Restricciones activas en PostgreSQL (claves primarias, foráneas, únicas y de control): "
                + claves);
        for (Locacion l : locaciones.listarTodos()) {
            List<Ocupacion> reservas = reglas.ocupaciones(l, null, Collections.<Integer>emptySet(),
                    Collections.<Ocupacion>emptyList());
            for (int i = 0; i < reservas.size(); i++) {
                Ocupacion o = reservas.get(i);
                if (o.getPersonas() > l.getCapacidad()) {
                    informe.add("Capacidad superada en " + l.getNombre() + ": " + o.getDescripcion());
                    problemas++;
                }
                Evento e = o.getEvento();
                List<LocalDate> dias = e == null ? Collections.singletonList(o.getDia())
                        : ReglasPlanificacion.dias(new Intervalo(e.getInicio(), e.getFin()));
                for (LocalDate d : dias) {
                    if (l.bloqueoEn(d) != null) {
                        informe.add("Reserva en fecha bloqueada de " + l.getNombre() + ": " + o.getDescripcion());
                        problemas++;
                    }
                }
                for (int j = i + 1; j < reservas.size(); j++) {
                    if (ReglasPlanificacion.chocan(o, reservas.get(j))) {
                        informe.add("Reservas superpuestas en " + l.getNombre() + ": " + o.getDescripcion()
                                + " y " + reservas.get(j).getDescripcion());
                        problemas++;
                    }
                }
            }
        }
        informe.add(problemas == 0
                ? "Reglas de planificación: sin superposiciones, sin capacidad superada y sin reservas en fechas"
                        + " bloqueadas."
                : "Se encontraron " + problemas + " problema(s) en las reglas de planificación.");
        return informe;
    }

    private Path carpetaRespaldos() {
        return Paths.get(bd.getConfiguracion().getCarpetaRespaldos()).toAbsolutePath();
    }

    private Path prepararCarpeta() {
        Path carpeta = carpetaRespaldos();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new ErrorBaseDatosException("No se pudo crear la carpeta de respaldos " + carpeta, e);
        }
        return carpeta;
    }

    /** Marca que la base tiene los datos de ejemplo (para mostrar los usuarios de prueba en el login). */
    public void marcarDatosDeEjemplo() {
        ctx.enTransaccion(c -> {
            try (Statement st = c.createStatement()) {
                st.executeUpdate("UPDATE control SET datos_ejemplo = true WHERE id = 1");
            }
            return null;
        });
    }

    public boolean tieneDatosDeEjemplo() {
        return ctx.leer(c -> {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT datos_ejemplo FROM control WHERE id = 1")) {
                return rs.next() && rs.getBoolean(1);
            }
        });
    }

    private void verificar(Usuario actor) {
        if (actor == null || !gestorUsuarios.tieneSesionActiva(actor)) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta acción");
        }
        if (!actor.tienePermiso(Permiso.GESTIONAR_BD)) {
            throw new AccesoDenegadoException("No tiene permisos para realizar esta acción");
        }
    }
}
