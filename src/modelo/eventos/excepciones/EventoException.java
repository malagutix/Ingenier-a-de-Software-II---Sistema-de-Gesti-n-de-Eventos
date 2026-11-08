package modelo.eventos.excepciones;

/** Excepción base del módulo de eventos y solicitudes. */
public class EventoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EventoException(String mensaje) {
        super(mensaje);
    }
}
