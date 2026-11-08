package modelo.eventos.entidades;

import java.time.LocalDate;
import java.time.LocalTime;

import modelo.eventos.entidades.estado.EstadoPendienteDeReserva;
import modelo.usuarios.entidades.Usuario;

/**
 * Patrón Factory Method: único lugar donde se construyen los eventos.
 * Arma cada evento de una planificación a partir de su solicitud. Si no se
 * indican hora o duración, usa las sugeridas por el tipo de evento.
 */
public class EventoFactory {

    public Evento crearEvento(Planificacion planificacion, LocalDate fecha, LocalTime horaInicio,
                              Integer duracionHoras, Usuario organizador) {
        SolicitudEvento solicitud = planificacion.getSolicitud();
        TipoEvento tipo = solicitud.getTipoEvento();
        return new Evento(
                fecha,
                horaInicio != null ? horaInicio : tipo.getHoraInicioSugerida(),
                duracionHoras != null ? duracionHoras : tipo.getDuracionSugerida(),
                solicitud.getCantidadInvitados(),
                tipo.getRequerimientosSugeridos(),
                tipo,
                solicitud.getLocacion(),
                planificacion,
                organizador,
                new EstadoPendienteDeReserva());
    }
}
