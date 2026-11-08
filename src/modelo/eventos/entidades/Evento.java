package modelo.eventos.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.eventos.entidades.estado.EstadoAceptado;
import modelo.eventos.entidades.estado.EstadoEvento;
import modelo.eventos.entidades.estado.TransicionEvento;
import modelo.usuarios.entidades.Observer;
import modelo.usuarios.entidades.Usuario;

/**
 * Evento planificado a partir de una solicitud. Siempre pertenece a una
 * Planificacion, que agrupa todos los eventos generados desde esa solicitud.
 * - Patrón State: delega en EstadoEvento qué transiciones y cambios admite.
 * - Patrón Observer: es el sujeto; avisa a sus observadores cada vez que cambia.
 */
public class Evento {

    private int idEvento;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private int duracionHoras;
    private int cantidadAsistentes;
    private String requerimientosEspecificos;
    private TipoEvento tipo;
    private Locacion locacion;
    private EstadoEvento estado;
    private boolean fueAceptado; // estuvo ACEPTADO alguna vez (aunque después se reprogramara)
    private final Planificacion planificacion;
    private final Usuario organizador;
    private HojaServicio hojaServicio; // composición 1 a 0..1
    private final List<Observer> observadores = new ArrayList<>();

    public Evento(LocalDate fecha, LocalTime horaInicio, int duracionHoras, int cantidadAsistentes, String requerimientosEspecificos,
                  TipoEvento tipo, Locacion locacion, Planificacion planificacion, Usuario organizador,
                  EstadoEvento estadoInicial) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracionHoras = duracionHoras;
        this.cantidadAsistentes = cantidadAsistentes;
        this.requerimientosEspecificos = requerimientosEspecificos;
        this.tipo = tipo;
        this.locacion = locacion;
        this.planificacion = planificacion;
        this.organizador = organizador;
        this.estado = estadoInicial;
    }

    // ---------------------------------------------------------------
    // Observer (sujeto)
    // ---------------------------------------------------------------

    public void agregarObserver(Observer observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void quitarObserver(Observer observador) {
        observadores.remove(observador);
    }

    public void quitarTodosLosObservers() {
        observadores.clear();
    }

    public void notificar(String mensaje) {
        for (Observer observador : new ArrayList<>(observadores)) {
            observador.actualizar(mensaje);
        }
    }

    // ---------------------------------------------------------------
    // State
    // ---------------------------------------------------------------

    /** Aplica una transición. La fecha nueva solo se usa al reprogramar. */
    public void aplicar(TransicionEvento transicion, LocalDate nuevaFecha) {
        estado.verificarAdmiteCambios();
        EstadoEvento nuevo;
        switch (transicion) {
            case ACEPTAR:
                nuevo = estado.aceptar();
                break;
            case INICIAR:
                nuevo = estado.iniciar();
                break;
            case FINALIZAR:
                nuevo = estado.finalizar();
                break;
            case REPROGRAMAR:
                nuevo = estado.reprogramar();
                break;
            case CANCELAR:
                nuevo = estado.cancelar();
                break;
            default:
                throw new IllegalArgumentException("Transición desconocida: " + transicion);
        }
        String anterior = estado.getNombre();
        String descripcion = descripcionCorta(); // antes de cambiar la fecha
        String detalle = "";
        if (transicion == TransicionEvento.REPROGRAMAR) {
            detalle = " (nueva fecha: " + Formato.fecha(nuevaFecha) + ")";
            this.fecha = nuevaFecha;
        }
        this.estado = nuevo;
        if (nuevo instanceof EstadoAceptado) {
            fueAceptado = true;
        }
        notificar(descripcion + " pasó de " + anterior + " a " + nuevo.getNombre() + detalle);
    }

    /** Lanza una excepción si el evento no se puede eliminar (CDU 012); lo decide su estado. */
    public void verificarEliminable() {
        estado.verificarEliminable(fueAceptado);
    }

    public void modificar(TipoEvento tipo, LocalTime horaInicio, int duracionHoras, int cantidadAsistentes,
                          Locacion locacion, String requerimientosEspecificos) {
        estado.verificarModificable();
        this.tipo = tipo;
        this.horaInicio = horaInicio;
        this.duracionHoras = duracionHoras;
        this.cantidadAsistentes = cantidadAsistentes;
        this.locacion = locacion;
        this.requerimientosEspecificos = requerimientosEspecificos;
        notificar(descripcionCorta() + " fue modificado por el organizador");
    }

    /** Fecha y hora de inicio. */
    /**
     * Restaura los datos leídos de la base de datos (solo para la persistencia).
     * No valida el estado ni notifica, porque no es un cambio sino una lectura.
     */
    public void restaurar(LocalDate fecha, LocalTime horaInicio, int duracionHoras, int cantidadAsistentes,
                          String requerimientosEspecificos, TipoEvento tipo, Locacion locacion,
                          EstadoEvento estado) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.duracionHoras = duracionHoras;
        this.cantidadAsistentes = cantidadAsistentes;
        this.requerimientosEspecificos = requerimientosEspecificos;
        this.tipo = tipo;
        this.locacion = locacion;
        this.estado = estado;
    }

    public LocalDateTime getInicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    /** Fecha y hora de fin (puede ser al día siguiente). */
    public LocalDateTime getFin() {
        return getInicio().plusHours(duracionHoras);
    }

    /**
     * Período en que el evento ocupa su locación: incluye el montaje antes del
     * inicio y el desmontaje después del fin (tiempo de montaje de la locación).
     */
    public Intervalo getIntervaloOcupacion() {
        int montaje = locacion.getTiempoMontajeMin();
        return new Intervalo(getInicio().minusMinutes(montaje), getFin().plusMinutes(montaje));
    }

    public String getHorario() {
        return Formato.horario(getInicio(), getFin());
    }

    /** Versión corta para tablas: "20:00 a 03:00 (+1)". */
    public String getHorarioCorto() {
        return getHorario().replace(" día)", ")");
    }

    // ---------------------------------------------------------------
    // Hoja de servicio (CDU 015)
    // ---------------------------------------------------------------

    public HojaServicio generarHojaServicio() {
        this.hojaServicio = new HojaServicio(this);
        return hojaServicio;
    }

    public String descripcionCorta() {
        return "El evento #" + idEvento + " (" + tipo + ", " + Formato.fecha(fecha) + ")";
    }

    public int getIdEvento() { return idEvento; }
    public void setIdEvento(int idEvento) { this.idEvento = idEvento; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public int getDuracionHoras() { return duracionHoras; }
    public int getCantidadAsistentes() { return cantidadAsistentes; }
    public String getRequerimientosEspecificos() { return requerimientosEspecificos; }
    public TipoEvento getTipo() { return tipo; }
    public Locacion getLocacion() { return locacion; }
    public EstadoEvento getEstado() { return estado; }
    public boolean fueAceptado() { return fueAceptado; }

    /** Restaura si el evento estuvo aceptado alguna vez (solo para la persistencia). */
    public void restaurarFueAceptado(boolean fueAceptado) { this.fueAceptado = fueAceptado; }
    public Planificacion getPlanificacion() { return planificacion; }
    public SolicitudEvento getSolicitud() { return planificacion.getSolicitud(); }
    public Usuario getOrganizador() { return organizador; }
    public Usuario getCliente() { return getSolicitud().getCliente(); }
    public MenuEvento getMenu() { return getSolicitud().getMenu(); }
    public HojaServicio getHojaServicio() { return hojaServicio; }
}
