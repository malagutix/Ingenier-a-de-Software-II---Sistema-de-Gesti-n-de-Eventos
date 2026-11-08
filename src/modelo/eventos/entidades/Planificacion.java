package modelo.eventos.entidades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import modelo.eventos.entidades.estado.EstadoCancelado;
import modelo.eventos.entidades.estado.EstadoEnProgreso;
import modelo.usuarios.entidades.Usuario;

/**
 * Planificación general: agrupa los eventos que se generan a partir de una
 * misma solicitud (por ejemplo, un congreso de varios días). Los eventos
 * comparten cliente, tipo, menú y locación de origen, y se pueden
 * reprogramar o cancelar en bloque.
 *
 * Relaciones: SolicitudEvento 1 -- 0..1 Planificacion 1 -- 1..* Evento
 */
public class Planificacion {

    private int idPlanificacion;
    private String nombre;
    private final SolicitudEvento solicitud;
    private final List<Evento> eventos = new ArrayList<>();

    public Planificacion(String nombre, SolicitudEvento solicitud) {
        this.nombre = nombre;
        this.solicitud = solicitud;
    }

    public void agregarEvento(Evento evento) {
        eventos.add(evento);
    }

    public void quitarEvento(Evento evento) {
        eventos.remove(evento);
    }

    /** Reemplaza los eventos por los leídos de la base de datos (solo para la persistencia). */
    public void restaurarEventos(List<Evento> guardados) {
        eventos.clear();
        eventos.addAll(guardados);
    }

    public void renombrar(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }

    /** Eventos ordenados por fecha. */
    public List<Evento> getEventos() {
        List<Evento> ordenados = new ArrayList<>(eventos);
        ordenados.sort(Comparator.comparing(Evento::getFecha));
        return ordenados;
    }

    /** Eventos que todavía no terminaron ni se cancelaron. */
    public List<Evento> getEventosActivos() {
        return getEventos().stream().filter(e -> !e.getEstado().esFinal()).collect(Collectors.toList());
    }

    public boolean estaVacia() {
        return eventos.isEmpty();
    }

    public LocalDate getFechaInicio() {
        return eventos.stream().map(Evento::getFecha).min(LocalDate::compareTo).orElse(null);
    }

    public LocalDate getFechaFin() {
        return eventos.stream().map(Evento::getFecha).max(LocalDate::compareTo).orElse(null);
    }

    /** Fecha de inicio considerando solo los eventos activos (se usa al reprogramar en bloque). */
    public LocalDate getFechaInicioActiva() {
        return getEventosActivos().stream().map(Evento::getFecha).min(LocalDate::compareTo).orElse(null);
    }

    /** Posición del evento dentro de la planificación, por ejemplo "2 de 3". */
    public String posicionDe(Evento evento) {
        return (getEventos().indexOf(evento) + 1) + " de " + eventos.size();
    }

    /**
     * Estado general calculado a partir de los estados de sus eventos:
     * CANCELADA, FINALIZADA, EN CURSO o PROGRAMADA.
     */
    public String getEstadoGeneral() {
        if (eventos.isEmpty()) {
            return "SIN EVENTOS";
        }
        boolean todosFinales = eventos.stream().allMatch(e -> e.getEstado().esFinal());
        if (todosFinales) {
            boolean todosCancelados = eventos.stream().allMatch(e -> e.getEstado() instanceof EstadoCancelado);
            return todosCancelados ? "CANCELADA" : "FINALIZADA";
        }
        boolean enCurso = eventos.stream().anyMatch(e -> e.getEstado() instanceof EstadoEnProgreso);
        return enCurso ? "EN CURSO" : "PROGRAMADA";
    }

    public int getIdPlanificacion() { return idPlanificacion; }
    public void setIdPlanificacion(int id) { this.idPlanificacion = id; }
    public String getNombre() { return nombre; }
    public SolicitudEvento getSolicitud() { return solicitud; }
    public Usuario getCliente() { return solicitud.getCliente(); }
    public int getCantidadEventos() { return eventos.size(); }

    @Override
    public String toString() {
        return "#" + idPlanificacion + " " + nombre;
    }
}
