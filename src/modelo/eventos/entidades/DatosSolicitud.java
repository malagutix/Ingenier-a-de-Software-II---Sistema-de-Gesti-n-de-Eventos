package modelo.eventos.entidades;

import java.time.LocalDate;

/** Datos del formulario de solicitud (RF008, RF009). */
public final class DatosSolicitud {

    private final Integer idCliente;
    private final LocalDate fechaSolicitada;
    private final Integer idTipoEvento;
    private final Integer cantidadInvitados;
    private final Integer idLocacion;
    private final MenuEvento menu;

    public DatosSolicitud(Integer idCliente, LocalDate fechaSolicitada, Integer idTipoEvento,
                          Integer cantidadInvitados, Integer idLocacion, MenuEvento menu) {
        this.idCliente = idCliente;
        this.fechaSolicitada = fechaSolicitada;
        this.idTipoEvento = idTipoEvento;
        this.cantidadInvitados = cantidadInvitados;
        this.idLocacion = idLocacion;
        this.menu = menu;
    }

    public Integer idCliente() { return idCliente; }
    public LocalDate fechaSolicitada() { return fechaSolicitada; }
    public Integer idTipoEvento() { return idTipoEvento; }
    public Integer cantidadInvitados() { return cantidadInvitados; }
    public Integer idLocacion() { return idLocacion; }
    public MenuEvento menu() { return menu; }
}
