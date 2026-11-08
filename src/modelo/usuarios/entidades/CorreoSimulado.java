package modelo.usuarios.entidades;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Correo de recuperación guardado en la bandeja simulada (cuando no hay servidor de correo). */
public final class CorreoSimulado {

    private final String destinatario;
    private final String codigo;
    private final LocalTime hora;

    public CorreoSimulado(String destinatario, String codigo, LocalTime hora) {
        this.destinatario = destinatario;
        this.codigo = codigo;
        this.hora = hora;
    }

    public String destinatario() { return destinatario; }
    public String codigo() { return codigo; }
    public LocalTime hora() { return hora; }

    @Override
    public String toString() {
        return hora.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                + "  -  Para: " + destinatario + "  -  Recuperación de contraseña";
    }
}
