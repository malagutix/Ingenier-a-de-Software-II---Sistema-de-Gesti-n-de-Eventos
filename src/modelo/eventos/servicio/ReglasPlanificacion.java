package modelo.eventos.servicio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Intervalo;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.excepciones.DisponibilidadException;
import modelo.eventos.repositorio.EventoRepository;
import modelo.eventos.repositorio.LocacionRepository;
import modelo.eventos.repositorio.PlanificacionRepository;
import modelo.eventos.repositorio.SolicitudRepository;

/**
 * Reglas de planificación: fechas, conflictos y disponibilidad de locaciones.
 *
 * - Un evento activo (no cancelado ni finalizado) reserva su horario exacto,
 *   más el tiempo de montaje antes y de desmontaje después.
 * - Una solicitud aceptada que todavía no se planificó reserva su día: ese día
 *   no puede empezar otro evento ni haber otra solicitud en esa locación.
 * - Dos eventos de la misma locación no pueden superponerse.
 * - Además se controla la capacidad, que la locación esté habilitada y que no
 *   tenga la fecha bloqueada.
 */
public class ReglasPlanificacion {

    private final SolicitudRepository solicitudes;
    private final PlanificacionRepository planificaciones;
    private final EventoRepository eventos;
    private final LocacionRepository locaciones;

    public ReglasPlanificacion(SolicitudRepository solicitudes, PlanificacionRepository planificaciones,
                               EventoRepository eventos, LocacionRepository locaciones) {
        this.solicitudes = solicitudes;
        this.planificaciones = planificaciones;
        this.eventos = eventos;
        this.locaciones = locaciones;
    }

    /** Reserva de una solicitud sin planificar: el día solicitado. */
    public static Ocupacion reservaDia(Locacion locacion, LocalDate fecha, int personas, String descripcion) {
        return new Ocupacion(locacion.getIdLocacion(), fecha, null, descripcion, personas, null);
    }

    /** Reserva de un evento: su horario más montaje y desmontaje de la locación. */
    public static Ocupacion reservaEvento(Locacion locacion, LocalDate fecha, LocalTime hora, int duracionHoras,
                                          int personas, String descripcion) {
        int montaje = locacion.getTiempoMontajeMin();
        Intervalo intervalo = new Intervalo(fecha.atTime(hora).minusMinutes(montaje),
                fecha.atTime(hora).plusHours(duracionHoras).plusMinutes(montaje));
        return new Ocupacion(locacion.getIdLocacion(), fecha, intervalo, descripcion, personas, null);
    }

    /** Indica si dos reservas de la misma locación chocan. */
    public static boolean chocan(Ocupacion a, Ocupacion b) {
        if (a.intervalo != null && b.intervalo != null) {
            return a.intervalo.seSuperponeCon(b.intervalo);
        }
        return a.dia.equals(b.dia);
    }

    /**
     * Verifica que la locación pueda recibir la reserva nueva. Si no puede,
     * lanza DisponibilidadException con las locaciones alternativas.
     *
     * @param dias       días que abarca la actividad (para los bloqueos)
     * @param pendientes reservas que se están creando en la misma operación
     */
    public void verificar(Locacion locacion, Ocupacion nueva, List<LocalDate> dias, Integer excluirSolicitud,
                          Collection<Integer> excluirEventos, List<Ocupacion> pendientes) {
        String problema = problema(locacion, nueva, dias, excluirSolicitud, excluirEventos, pendientes);
        if (problema == null) {
            return;
        }
        List<Locacion> alternativas = new ArrayList<>();
        for (Locacion l : locaciones.listarTodos()) {
            if (l.getIdLocacion() != locacion.getIdLocacion()) {
                Ocupacion enOtra = nueva.intervalo == null
                        ? new Ocupacion(l.getIdLocacion(), nueva.dia, null, nueva.descripcion, nueva.personas, null)
                        : trasladar(nueva, locacion, l);
                if (problema(l, enOtra, dias, excluirSolicitud, excluirEventos, pendientes) == null) {
                    alternativas.add(l);
                }
            }
        }
        String cuando = Formato.fecha(nueva.dia);
        String sugerencia = alternativas.isEmpty()
                ? "No hay otras locaciones disponibles para " + nueva.personas + " personas el " + cuando
                        + ". Probá con otra fecha u horario."
                : "Locaciones disponibles para el " + cuando + ":\n"
                        + alternativas.stream().map(l -> "  - " + l).collect(Collectors.joining("\n"));
        throw new DisponibilidadException("No hay disponibilidad. " + problema + "\n\n" + sugerencia);
    }

    /** La misma reserva de evento, pero con el tiempo de montaje de otra locación. */
    private static Ocupacion trasladar(Ocupacion reserva, Locacion origen, Locacion destino) {
        int montajeOrigen = origen.getTiempoMontajeMin();
        int montajeDestino = destino.getTiempoMontajeMin();
        Intervalo intervalo = new Intervalo(
                reserva.intervalo.getDesde().plusMinutes(montajeOrigen).minusMinutes(montajeDestino),
                reserva.intervalo.getHasta().minusMinutes(montajeOrigen).plusMinutes(montajeDestino));
        return new Ocupacion(destino.getIdLocacion(), reserva.dia, intervalo, reserva.descripcion,
                reserva.personas, null);
    }

    /** Devuelve el motivo por el que no se puede usar la locación, o null si se puede. */
    private String problema(Locacion locacion, Ocupacion nueva, List<LocalDate> dias, Integer excluirSolicitud,
                            Collection<Integer> excluirEventos, List<Ocupacion> pendientes) {
        if (!locacion.isHabilitada()) {
            return "La locación " + locacion.getNombre() + " no está habilitada.";
        }
        if (nueva.personas > locacion.getCapacidad()) {
            return "La locación " + locacion.getNombre() + " tiene capacidad para "
                    + locacion.getCapacidad() + " personas.";
        }
        for (LocalDate dia : dias) {
            BloqueoFecha bloqueo = locacion.bloqueoEn(dia);
            if (bloqueo != null) {
                return "La locación " + locacion.getNombre() + " no está disponible el " + Formato.fecha(dia)
                        + " (" + bloqueo.getMotivo() + ").";
            }
        }
        for (Ocupacion o : ocupaciones(locacion, excluirSolicitud, excluirEventos, pendientes)) {
            if (chocan(o, nueva)) {
                return "La locación " + locacion.getNombre() + " ya está reservada por " + o.descripcion + ".";
            }
        }
        return null;
    }

    /** Todas las reservas actuales de una locación. */
    public List<Ocupacion> ocupaciones(Locacion locacion, Integer excluirSolicitud,
                                       Collection<Integer> excluirEventos, List<Ocupacion> pendientes) {
        List<Ocupacion> resultado = new ArrayList<>();
        for (Evento e : eventos.listarTodos()) {
            if (!e.getEstado().esFinal()
                    && e.getLocacion().getIdLocacion() == locacion.getIdLocacion()
                    && !excluirEventos.contains(e.getIdEvento())) {
                resultado.add(new Ocupacion(locacion.getIdLocacion(), e.getFecha(), e.getIntervaloOcupacion(),
                        "el evento #" + e.getIdEvento() + " (" + Formato.fecha(e.getFecha()) + ", " + e.getHorario()
                                + ", más " + Formato.minutos(locacion.getTiempoMontajeMin())
                                + " de montaje antes y después)",
                        e.getCantidadAsistentes(), e));
            }
        }
        for (SolicitudEvento s : solicitudes.listarTodos()) {
            if (s.estaVigente()
                    && s.getLocacion().getIdLocacion() == locacion.getIdLocacion()
                    && (excluirSolicitud == null || s.getIdSolicitud() != excluirSolicitud)
                    && !planificaciones.buscarPorSolicitud(s.getIdSolicitud()).isPresent()) {
                resultado.add(reservaDia(locacion, s.getFechaSolicitada(), s.getCantidadInvitados(),
                        "la solicitud #" + s.getIdSolicitud() + ", que reserva el "
                                + Formato.fecha(s.getFechaSolicitada())));
            }
        }
        for (Ocupacion p : pendientes) {
            if (p.idLocacion == locacion.getIdLocacion()) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /** Reservas activas que caen en un rango de fechas (para bloquear fechas). */
    public List<Ocupacion> ocupacionesEntre(Locacion locacion, LocalDate desde, LocalDate hasta) {
        Intervalo rango = new Intervalo(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        List<Ocupacion> resultado = new ArrayList<>();
        for (Ocupacion o : ocupaciones(locacion, null, Collections.<Integer>emptySet(),
                Collections.<Ocupacion>emptyList())) {
            boolean diaEnRango = !o.dia.isBefore(desde) && !o.dia.isAfter(hasta);
            if (diaEnRango || (o.intervalo != null && o.intervalo.seSuperponeCon(rango))) {
                resultado.add(o);
            }
        }
        return resultado;
    }

    /** Días que abarca un período (para controlar bloqueos). */
    public static List<LocalDate> dias(Intervalo periodo) {
        List<LocalDate> dias = new ArrayList<>();
        LocalDate dia = periodo.getDesde().toLocalDate();
        LocalDate ultimo = periodo.getHasta().minusNanos(1).toLocalDate();
        while (!dia.isAfter(ultimo)) {
            dias.add(dia);
            dia = dia.plusDays(1);
        }
        return dias;
    }

    /** Una reserva de una locación: un evento (con horario) o una solicitud (día completo). */
    public static final class Ocupacion {
        private final int idLocacion;
        private final LocalDate dia;
        private final Intervalo intervalo;
        private final String descripcion;
        private final int personas;
        private final Evento evento;

        Ocupacion(int idLocacion, LocalDate dia, Intervalo intervalo, String descripcion, int personas,
                  Evento evento) {
            this.idLocacion = idLocacion;
            this.dia = dia;
            this.intervalo = intervalo;
            this.descripcion = descripcion;
            this.personas = personas;
            this.evento = evento;
        }

        /** Copia de una reserva de evento con otro horario (para recalcular el montaje). */
        Ocupacion conIntervalo(Intervalo nuevo) {
            return new Ocupacion(idLocacion, dia, nuevo, descripcion, personas, evento);
        }

        public LocalDate getDia() { return dia; }
        /** Horario reservado, o null si es una solicitud (reserva el día). */
        public Intervalo getIntervalo() { return intervalo; }
        public String getDescripcion() { return descripcion; }
        public int getPersonas() { return personas; }
        /** El evento que reserva, o null si es una solicitud sin planificar. */
        public Evento getEvento() { return evento; }
    }
}
