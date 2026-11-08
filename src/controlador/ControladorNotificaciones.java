package controlador;

import java.util.List;

import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.servicio.GestorUsuarios;
import vista.VistaNotificaciones;

/**
 * Controlador de las notificaciones del usuario (patrón Observer de los
 * eventos): mostrarlas, marcarlas como leídas y eliminarlas.
 */
public class ControladorNotificaciones extends Controlador implements Refrescable {

    private final VistaNotificaciones vista;
    private final GestorUsuarios gestor;
    private final Usuario usuario;
    private final Runnable alCambiar;

    /** @param alCambiar qué hacer cuando cambia la cantidad de no leídas (actualizar el título de la pestaña) */
    public ControladorNotificaciones(VistaNotificaciones vista, GestorUsuarios gestor, Usuario usuario,
                                     Runnable alCambiar) {
        this.vista = vista;
        this.gestor = gestor;
        this.usuario = usuario;
        this.alCambiar = alCambiar;
        vista.alMarcarLeidas(() -> marcarLeidas(vista.getSeleccionadas()));
        vista.alMarcarTodasLeidas(() -> marcarLeidas(vista.getNoLeidas()));
        vista.alEliminar(() -> eliminar(vista.getSeleccionadas(), false));
        vista.alEliminarTodas(() -> eliminar(vista.getTodas(), true));
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<Notificacion> notificaciones = cargar(vista, () -> gestor.obtenerNotificaciones(usuario));
        if (notificaciones != null) {
            vista.mostrarNotificaciones(notificaciones);
            vista.mostrarEstado(mensajeExito);
        }
    }

    private void marcarLeidas(List<Notificacion> notificaciones) {
        if (notificaciones.isEmpty()) {
            return;
        }
        if (ejecutar(vista, () -> gestor.marcarNotificacionesLeidas(usuario, notificaciones))) {
            refrescar(notificaciones.size() == 1 ? "Notificación marcada como leída"
                    : notificaciones.size() + " notificaciones marcadas como leídas");
            alCambiar.run();
        }
    }

    private void eliminar(List<Notificacion> notificaciones, boolean todas) {
        if (notificaciones.isEmpty()) {
            return;
        }
        String cuales = todas ? "todas las notificaciones (" + notificaciones.size() + ")"
                : notificaciones.size() == 1 ? "la notificación seleccionada"
                : "las " + notificaciones.size() + " notificaciones seleccionadas";
        if (vista.confirmar("¿Eliminar " + cuales + "?\nEsta acción no se puede deshacer.", "Eliminar notificaciones")
                && ejecutar(vista, () -> gestor.eliminarNotificaciones(usuario, notificaciones))) {
            refrescar(notificaciones.size() == 1 ? "Notificación eliminada"
                    : notificaciones.size() + " notificaciones eliminadas");
            alCambiar.run();
        }
    }
}
