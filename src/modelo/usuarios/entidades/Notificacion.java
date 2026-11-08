package modelo.usuarios.entidades;

import java.time.LocalDateTime;

/** Aviso que recibe un usuario como Observer de un evento. Se puede marcar como leído o eliminar. */
public class Notificacion {

    private int idNotificacion; // 0 mientras no se guardó en la base de datos
    private final String mensaje;
    private final LocalDateTime creada;
    private boolean leida;

    public Notificacion(String mensaje) {
        this(0, mensaje, LocalDateTime.now(), false);
    }

    /** Restaura una notificación guardada (solo para la persistencia). */
    public Notificacion(int idNotificacion, String mensaje, LocalDateTime creada, boolean leida) {
        this.idNotificacion = idNotificacion;
        this.mensaje = mensaje;
        this.creada = creada;
        this.leida = leida;
    }

    public void marcarLeida() {
        this.leida = true;
    }

    /** Indica si es la misma notificación (por ID si ya está guardada; si no, el mismo objeto). */
    public boolean esLaMisma(Notificacion otra) {
        return idNotificacion != 0 ? idNotificacion == otra.idNotificacion : this == otra;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int id) { this.idNotificacion = id; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getCreada() { return creada; }
    public boolean isLeida() { return leida; }

    @Override
    public String toString() {
        return mensaje;
    }
}
