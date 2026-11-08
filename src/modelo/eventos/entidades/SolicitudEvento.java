package modelo.eventos.entidades;

import java.time.LocalDate;

import modelo.usuarios.entidades.Usuario;

/** Requerimientos iniciales de un cliente para un evento futuro (RF008). */
public class SolicitudEvento {

    private int idSolicitud;
    private LocalDate fechaSolicitada;
    private TipoEvento tipoEvento;
    private int cantidadInvitados;
    private Locacion locacion;
    private MenuEvento menu;
    private Usuario cliente;
    private EstadoSolicitud estado;

    public SolicitudEvento(Usuario cliente, LocalDate fechaSolicitada, TipoEvento tipoEvento,
                           int cantidadInvitados, Locacion locacion, MenuEvento menu) {
        this.cliente = cliente;
        this.fechaSolicitada = fechaSolicitada;
        this.tipoEvento = tipoEvento;
        this.cantidadInvitados = cantidadInvitados;
        this.locacion = locacion;
        this.menu = menu;
        this.estado = EstadoSolicitud.PENDIENTE; // CDU 008, paso 4
    }

    public void modificar(Usuario cliente, LocalDate fecha, TipoEvento tipo, int invitados,
                          Locacion locacion, MenuEvento menu) {
        this.cliente = cliente;
        this.fechaSolicitada = fecha;
        this.tipoEvento = tipo;
        this.cantidadInvitados = invitados;
        this.locacion = locacion;
        this.menu = menu;
    }

    public void aceptar() {
        this.estado = EstadoSolicitud.ACEPTADA;
    }

    public void rechazar() {
        this.estado = EstadoSolicitud.RECHAZADA;
    }

    /** Se quita de la lista de solicitudes pero se conserva para sus eventos. */
    public void marcarEliminada() {
        this.estado = EstadoSolicitud.ELIMINADA;
    }

    /** Restaura el estado leído de la base de datos (solo para la persistencia). */
    public void restaurarEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public boolean estaAceptada() {
        return estado == EstadoSolicitud.ACEPTADA;
    }

    public boolean estaPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    /** Pendiente o aceptada: reserva su día y se puede modificar. */
    public boolean estaVigente() {
        return estado == EstadoSolicitud.PENDIENTE || estado == EstadoSolicitud.ACEPTADA;
    }

    public boolean estaEliminada() {
        return estado == EstadoSolicitud.ELIMINADA;
    }

    public int getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(int idSolicitud) { this.idSolicitud = idSolicitud; }
    public LocalDate getFechaSolicitada() { return fechaSolicitada; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public int getCantidadInvitados() { return cantidadInvitados; }
    public Locacion getLocacion() { return locacion; }
    public MenuEvento getMenu() { return menu; }
    public Usuario getCliente() { return cliente; }
    public EstadoSolicitud getEstado() { return estado; }

    @Override
    public String toString() {
        return "#" + idSolicitud + "  " + Formato.fecha(fechaSolicitada) + "  " + tipoEvento
                + "  " + cantidadInvitados + " invitados  " + locacion.getNombre()
                + "  -  " + cliente.getNombre() + " " + cliente.getApellido();
    }
}
