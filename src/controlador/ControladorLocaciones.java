package controlador;

import java.util.List;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.servicio.GestorCatalogos;
import modelo.usuarios.entidades.Usuario;
import vista.VistaLocaciones;

/** Controlador de la gestión de locaciones: alta, modificación, habilitación, eliminación y bloqueo de fechas. */
public class ControladorLocaciones extends Controlador implements Refrescable {

    private final VistaLocaciones vista;
    private final GestorCatalogos catalogos;
    private final Usuario admin;

    public ControladorLocaciones(VistaLocaciones vista, GestorCatalogos catalogos, Usuario admin) {
        this.vista = vista;
        this.catalogos = catalogos;
        this.admin = admin;
        vista.alNueva(this::crear);
        vista.alModificar(this::modificar);
        vista.alCambiarHabilitacion(this::cambiarHabilitacion);
        vista.alEliminar(this::eliminar);
        vista.alBloquear(this::bloquear);
        vista.alDesbloquear(this::desbloquear);
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<Locacion> locaciones = cargar(vista, () -> catalogos.listarLocaciones(admin));
        if (locaciones == null) {
            return;
        }
        vista.mostrarLocaciones(locaciones);
        vista.mostrarEstado(mensajeExito);
    }

    private void crear() {
        Locacion[] creada = new Locacion[1];
        if (vista.editarLocacion("Nueva locación", null,
                datos -> ejecutar(vista, () -> creada[0] = catalogos.crearLocacion(admin, datos)))) {
            refrescar("Locación \"" + creada[0].getNombre() + "\" creada");
            vista.seleccionar(creada[0]);
        }
    }

    private void modificar() {
        Locacion l = vista.getSeleccionada();
        if (l == null) {
            return;
        }
        if (vista.editarLocacion("Modificar locación #" + l.getIdLocacion(), l,
                datos -> ejecutar(vista, () -> catalogos.modificarLocacion(admin, l.getIdLocacion(), datos)))) {
            refrescar("Locación modificada");
        }
    }

    /** Inhabilitar no afecta lo ya planificado: solo impide usarla en solicitudes y eventos nuevos. */
    private void cambiarHabilitacion() {
        Locacion l = vista.getSeleccionada();
        if (l == null) {
            return;
        }
        boolean habilitar = !l.isHabilitada();
        if (!habilitar && !vista.confirmar("¿Inhabilitar " + l.getNombre() + "?\n"
                + "No se va a poder elegir en solicitudes ni eventos nuevos.\n"
                + "Lo que ya está planificado en ella no cambia.", "Inhabilitar locación")) {
            return;
        }
        Integer reservas = consultar(vista, () -> catalogos.cambiarHabilitacion(admin, l.getIdLocacion(), habilitar));
        if (reservas != null) {
            String extra = !habilitar && reservas > 0 ? " (mantiene " + reservas + " reserva(s) activa(s))" : "";
            refrescar((habilitar ? "Locación habilitada" : "Locación inhabilitada") + extra);
        }
    }

    private void eliminar() {
        Locacion l = vista.getSeleccionada();
        if (l == null) {
            return;
        }
        if (vista.confirmar("¿Eliminar " + l.getNombre() + "?", "Eliminar locación")
                && ejecutar(vista, () -> catalogos.eliminarLocacion(admin, l.getIdLocacion()))) {
            refrescar("Locación eliminada");
        }
    }

    private void bloquear() {
        Locacion l = vista.getSeleccionada();
        if (l == null) {
            return;
        }
        if (vista.pedirBloqueo(l, (desde, hasta, motivo) -> ejecutar(vista,
                () -> catalogos.bloquearFechas(admin, l.getIdLocacion(), desde, hasta, motivo)))) {
            refrescar("Fechas bloqueadas");
        }
    }

    private void desbloquear() {
        Locacion l = vista.getSeleccionada();
        BloqueoFecha b = vista.getBloqueoSeleccionado();
        if (l == null || b == null) {
            return;
        }
        if (ejecutar(vista, () -> catalogos.desbloquear(admin, l.getIdLocacion(), b))) {
            refrescar("Bloqueo quitado");
        }
    }
}
