package controlador;

import java.time.LocalDate;
import java.util.List;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Planificacion;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import vista.VistaPlanificaciones;

/**
 * Controlador de la planificación de múltiples eventos: planificar (CDU 011),
 * agregar eventos, renombrar, reprogramar todo y cancelar todo.
 */
public class ControladorPlanificaciones extends Controlador implements Refrescable {

    private final VistaPlanificaciones vista;
    private final SistemaEventos sistema;
    private final Usuario organizador;
    private final PlanificadorEventos planificador;

    public ControladorPlanificaciones(VistaPlanificaciones vista, SistemaEventos sistema, Usuario organizador) {
        this.vista = vista;
        this.sistema = sistema;
        this.organizador = organizador;
        this.planificador = new PlanificadorEventos(sistema, organizador);
        vista.setPuedeValidar(organizador.tienePermiso(Permiso.VALIDAR_EVENTO));
        vista.alPlanificar(() -> planificar(null));
        vista.alAgregarEventos(() -> {
            Planificacion p = vista.getSeleccionada();
            if (p != null) {
                planificar(p.getSolicitud().getIdSolicitud());
            }
        });
        vista.alRenombrar(this::renombrar);
        vista.alReprogramar(this::reprogramar);
        vista.alCancelar(this::cancelar);
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<Planificacion> planificaciones = cargar(vista, () -> sistema.listarPlanificaciones(organizador));
        if (planificaciones == null) {
            return;
        }
        vista.mostrarPlanificaciones(planificaciones);
        vista.mostrarEstado(mensajeExito);
    }

    private void planificar(Integer idSolicitud) {
        List<Evento> creados = planificador.planificar(vista, idSolicitud);
        if (!creados.isEmpty()) {
            refrescar(PlanificadorEventos.mensajeExito(creados));
            vista.seleccionar(creados.get(0).getPlanificacion());
        }
    }

    private void renombrar() {
        Planificacion p = vista.getSeleccionada();
        if (p == null) {
            return;
        }
        String nombre = vista.pedirNombre(p.getNombre());
        if (nombre != null && ejecutar(vista,
                () -> sistema.renombrarPlanificacion(organizador, p.getIdPlanificacion(), nombre))) {
            refrescar("Planificación renombrada");
        }
    }

    private void reprogramar() {
        Planificacion p = vista.getSeleccionada();
        if (p == null) {
            return;
        }
        LocalDate inicio = p.getFechaInicioActiva();
        if (inicio == null) {
            vista.mostrarError("La planificación no tiene eventos activos para reprogramar");
            return;
        }
        LocalDate nueva = vista.pedirNuevaFechaInicio(p, inicio.plusDays(7));
        if (nueva != null && ejecutar(vista,
                () -> sistema.reprogramarPlanificacion(organizador, p.getIdPlanificacion(), nueva))) {
            refrescar("Planificación reprogramada: ahora empieza el " + Formato.fecha(p.getFechaInicioActiva()));
        }
    }

    private void cancelar() {
        Planificacion p = vista.getSeleccionada();
        if (p == null) {
            return;
        }
        if (vista.confirmar("¿Cancelar todos los eventos activos de \"" + p.getNombre()
                + "\"?\nEsta acción no se puede deshacer.", "Cancelar planificación")
                && ejecutar(vista, () -> sistema.cancelarPlanificacion(organizador, p.getIdPlanificacion()))) {
            refrescar("Planificación cancelada");
        }
    }
}
