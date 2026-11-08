package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Usuario;
import vista.DialogoPlanificar.OpcionSolicitud;
import vista.VistaConPlanificacion;

/**
 * Parte de controlador compartida por Planificaciones y Eventos: planificar
 * uno o varios eventos a partir de una solicitud (CDU 011).
 */
class PlanificadorEventos extends Controlador {

    private final SistemaEventos sistema;
    private final Usuario organizador;

    PlanificadorEventos(SistemaEventos sistema, Usuario organizador) {
        this.sistema = sistema;
        this.organizador = organizador;
    }

    /**
     * @param idSolicitud solicitud a preseleccionar, o null
     * @return los eventos creados (vacío si se canceló)
     */
    List<Evento> planificar(VistaConPlanificacion vista, Integer idSolicitud) {
        List<SolicitudEvento> solicitudes = cargar(vista, () -> sistema.listarSolicitudesParaPlanificar(organizador));
        if (solicitudes == null) {
            return Collections.emptyList();
        }
        if (solicitudes.isEmpty()) {
            vista.mostrarInfo("No hay solicitudes aceptadas.\nEl administrador las carga en la pestaña Solicitudes.");
            return Collections.emptyList();
        }
        List<OpcionSolicitud> opciones = new ArrayList<>();
        for (SolicitudEvento s : solicitudes) {
            opciones.add(new OpcionSolicitud(s, sistema.planificacionDeSolicitud(s.getIdSolicitud()).orElse(null),
                    sistema.fechaSugerida(s)));
        }
        List<Evento> creados = new ArrayList<>();
        vista.pedirPlanificacion(opciones, idSolicitud, p -> ejecutar(vista,
                () -> creados.addAll(sistema.planificarEventos(organizador, p.idSolicitud(), p.fechaInicio(),
                        p.horaInicio(), p.duracionHoras(), p.cantidadDias(), p.nombre()))));
        return creados;
    }

    static String mensajeExito(List<Evento> creados) {
        if (creados.size() == 1) {
            return "Evento #" + creados.get(0).getIdEvento() + " creado correctamente (PENDIENTE DE RESERVA)";
        }
        return creados.size() + " eventos creados en la planificación \"" + creados.get(0).getPlanificacion().getNombre()
                + "\"";
    }
}
