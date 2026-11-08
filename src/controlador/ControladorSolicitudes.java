package controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Usuario;
import vista.VistaSolicitudes;

/**
 * Controlador de solicitudes: CDU 008 (crear), CDU 009 (modificar),
 * CDU 010 (eliminar), aceptar y rechazar.
 */
public class ControladorSolicitudes extends Controlador implements Refrescable {

    private final VistaSolicitudes vista;
    private final SistemaEventos sistema;
    private final Usuario admin;

    public ControladorSolicitudes(VistaSolicitudes vista, SistemaEventos sistema, Usuario admin) {
        this.vista = vista;
        this.sistema = sistema;
        this.admin = admin;
        vista.alNueva(this::crear);
        vista.alModificar(this::modificar);
        vista.alAceptar(this::aceptar);
        vista.alRechazar(this::rechazar);
        vista.alEliminar(this::eliminar);
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<SolicitudEvento> solicitudes = cargar(vista, () -> sistema.listarSolicitudes(admin));
        if (solicitudes == null) {
            return;
        }
        Map<Integer, String> planificaciones = new HashMap<>();
        for (SolicitudEvento s : solicitudes) {
            sistema.planificacionDeSolicitud(s.getIdSolicitud()).ifPresent(p -> planificaciones.put(s.getIdSolicitud(),
                    "#" + p.getIdPlanificacion() + " (" + p.getCantidadEventos() + " ev.)"));
        }
        vista.mostrarSolicitudes(solicitudes, planificaciones);
        vista.mostrarEstado(mensajeExito);
    }

    // CDU 008 - Crear solicitud
    private void crear() {
        List<Usuario> clientes = cargar(vista, () -> sistema.listarClientes(admin));
        if (clientes == null) {
            return;
        }
        if (clientes.isEmpty()) {
            vista.mostrarError("No hay usuarios con rol CLIENTE.\nAsigná ese rol a un usuario desde la pestaña Usuarios.");
            return;
        }
        SolicitudEvento[] creada = new SolicitudEvento[1];
        if (vista.editarSolicitud("Nueva solicitud", clientes, sistema.listarTiposActivos(),
                sistema.listarLocacionesHabilitadas(), null,
                datos -> ejecutar(vista, () -> creada[0] = sistema.crearSolicitud(admin, datos)))) {
            refrescar("Solicitud #" + creada[0].getIdSolicitud() + " creada exitosamente");
        }
    }

    // CDU 009 - Modificar solicitud
    private void modificar() {
        SolicitudEvento s = vista.getSeleccionada();
        if (s == null) {
            return;
        }
        List<Usuario> clientes = cargar(vista, () -> sistema.listarClientes(admin));
        if (clientes == null) {
            return;
        }
        if (vista.editarSolicitud("Modificar solicitud #" + s.getIdSolicitud(), clientes,
                sistema.listarTiposActivos(), sistema.listarLocacionesHabilitadas(), s,
                datos -> ejecutar(vista, () -> sistema.modificarSolicitud(admin, s.getIdSolicitud(), datos)))) {
            refrescar("Solicitud modificada");
        }
    }

    private void aceptar() {
        SolicitudEvento s = vista.getSeleccionada();
        if (s == null) {
            return;
        }
        Evento[] creado = new Evento[1];
        if (vista.preguntar("¿Aceptar la solicitud #" + s.getIdSolicitud() + "?\nSe crea su evento el "
                        + Formato.fecha(s.getFechaSolicitada()) + " (después se le pueden agregar más días "
                        + "desde Planificaciones).",
                "Aceptar solicitud")
                && ejecutar(vista, () -> creado[0] = sistema.aceptarSolicitud(admin, s.getIdSolicitud()))) {
            refrescar("Solicitud #" + s.getIdSolicitud() + " aceptada: se creó el evento #"
                    + creado[0].getIdEvento() + " (PENDIENTE DE RESERVA)");
        }
    }

    private void rechazar() {
        SolicitudEvento s = vista.getSeleccionada();
        if (s == null) {
            return;
        }
        if (vista.preguntar("¿Rechazar la solicitud #" + s.getIdSolicitud() + "?", "Rechazar solicitud")
                && ejecutar(vista, () -> sistema.rechazarSolicitud(admin, s.getIdSolicitud()))) {
            refrescar("Solicitud rechazada");
        }
    }

    // CDU 010 - Eliminar solicitud: 1) se seleccionan una o más, 2) se verifica si son
    // parte de una planificación general, 3) se pide confirmación y se eliminan.
    private void eliminar() {
        List<SolicitudEvento> seleccionadas = vista.getSeleccionadas();
        if (seleccionadas.isEmpty()) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        List<String> planificadas = new ArrayList<>();
        int cantidadEventos = 0;
        for (SolicitudEvento s : seleccionadas) {
            ids.add(s.getIdSolicitud());
            int suyos = sistema.planificacionDeSolicitud(s.getIdSolicitud())
                    .map(p -> p.getCantidadEventos()).orElse(0);
            if (suyos > 0) {
                planificadas.add("#" + s.getIdSolicitud() + " (" + suyos + " ev.)");
                cantidadEventos += suyos;
            }
        }
        String cuales = seleccionadas.size() == 1 ? "la solicitud #" + ids.get(0)
                : "las " + ids.size() + " solicitudes seleccionadas (" + listar(ids) + ")";

        if (cantidadEventos == 0) {
            if (vista.confirmar("¿Eliminar " + cuales + "?\nEsta acción no se puede deshacer.",
                    "Eliminar solicitud")
                    && ejecutar(vista, () -> sistema.eliminarSolicitudes(admin, ids, true))) {
                refrescar(ids.size() == 1 ? "Solicitud eliminada" : ids.size() + " solicitudes eliminadas");
            }
            return;
        }

        // Paso 2: alguna es parte de una planificación general; se pregunta por sus eventos
        String eventos = cantidadEventos == 1 ? "1 evento" : cantidadEventos + " eventos";
        String detalle = seleccionadas.size() == 1
                ? "La solicitud #" + ids.get(0) + " es parte de una planificación general y tiene " + eventos + "."
                : "Son parte de una planificación general: " + String.join(", ", planificadas)
                        + ", con " + eventos + " en total.";
        Boolean conEventos = vista.preguntarSiNoCancelar("¿Eliminar " + cuales + "?\n\n" + detalle
                        + "\n¿Querés eliminar también " + (cantidadEventos == 1 ? "ese evento" : "esos eventos") + "?\n\n"
                        + "Sí: se eliminan las solicitudes y sus eventos.\n"
                        + "No: se eliminan las solicitudes y los eventos se conservan.\n"
                        + "Esta acción no se puede deshacer.",
                "Eliminar solicitud");
        if (conEventos != null
                && ejecutar(vista, () -> sistema.eliminarSolicitudes(admin, ids, conEventos))) {
            String solicitudes = ids.size() == 1 ? "Solicitud eliminada" : ids.size() + " solicitudes eliminadas";
            refrescar(conEventos ? solicitudes + " junto con " + eventos
                    : solicitudes + "; " + eventos + (cantidadEventos == 1 ? " se conserva" : " se conservan"));
        }
    }

    private static String listar(List<Integer> ids) {
        List<String> textos = new ArrayList<>();
        for (int id : ids) {
            textos.add("#" + id);
        }
        return String.join(", ", textos);
    }
}
