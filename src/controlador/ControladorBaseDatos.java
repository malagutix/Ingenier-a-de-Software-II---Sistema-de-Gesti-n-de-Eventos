package controlador;

import java.nio.file.Path;
import java.util.List;

import modelo.persistencia.ConsultaTabla;
import modelo.persistencia.EstadoBaseDatos;
import modelo.persistencia.GestorBaseDatos;
import modelo.usuarios.entidades.Usuario;
import vista.VistaBaseDatos;

/**
 * Controlador del CDU 007 - Gestionar base de datos: estado, consulta de
 * tablas, respaldo, restauración y verificación de integridad.
 */
public class ControladorBaseDatos extends Controlador implements Refrescable {

    private final VistaBaseDatos vista;
    private final GestorBaseDatos gestor;
    private final Usuario admin;
    private final Runnable alRestaurar;

    /**
     * @param gestor      null si el sistema trabaja en memoria (modo demostración)
     * @param alRestaurar se ejecuta después de restaurar un respaldo (hay que volver a iniciar sesión)
     */
    public ControladorBaseDatos(VistaBaseDatos vista, GestorBaseDatos gestor, Usuario admin, Runnable alRestaurar) {
        this.vista = vista;
        this.gestor = gestor;
        this.admin = admin;
        this.alRestaurar = alRestaurar;
        if (gestor == null) {
            return;
        }
        vista.alRespaldar(this::respaldar);
        vista.alRestaurar(this::restaurar);
        vista.alVerificar(this::verificar);
        vista.alVerRegistros(this::verRegistros);
        refrescar();
    }

    @Override
    public void refrescar() {
        if (gestor == null) {
            return;
        }
        EstadoBaseDatos estado = cargar(vista, () -> gestor.obtenerEstado(admin));
        if (estado != null) {
            vista.mostrarEstado(estado);
        }
    }

    private void verRegistros() {
        String tabla = vista.getTablaSeleccionada();
        if (tabla == null) {
            return;
        }
        ConsultaTabla consulta = cargar(vista, () -> gestor.consultarTabla(admin, tabla));
        if (consulta != null) {
            vista.mostrarRegistros(tabla, consulta);
        }
    }

    private void respaldar() {
        Path destino = vista.elegirDestinoRespaldo();
        if (destino == null) {
            return;
        }
        Path creado = conEspera(() -> gestor.respaldar(admin, destino));
        if (creado != null) {
            vista.mostrarResultado("Respaldo guardado en:\n" + creado);
            refrescar();
        }
    }

    private void restaurar() {
        Path origen = vista.elegirRespaldoARestaurar();
        if (origen == null || !vista.confirmar("Se van a reemplazar TODOS los datos actuales por los del respaldo:\n"
                + origen + "\n\nConviene hacer antes un respaldo de lo actual. ¿Continuar?", "Restaurar respaldo")) {
            return;
        }
        if (conEspera(() -> {
            gestor.restaurar(admin, origen);
            return Boolean.TRUE;
        }) != null) {
            vista.mostrarInfo("Datos restaurados. Volvé a iniciar sesión.");
            alRestaurar.run();
        }
    }

    private void verificar() {
        List<String> informe = conEspera(() -> gestor.verificarIntegridad(admin));
        if (informe != null) {
            vista.mostrarResultado(String.join("\n\n", informe));
        }
    }

    /** Las operaciones largas muestran el cursor de espera. */
    private <T> T conEspera(java.util.function.Supplier<T> operacion) {
        vista.ocupado(true);
        try {
            return consultar(vista, operacion);
        } finally {
            vista.ocupado(false);
        }
    }
}
