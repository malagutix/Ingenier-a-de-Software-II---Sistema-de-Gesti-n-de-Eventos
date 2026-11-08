package modelo.eventos.excepciones;

/** El estado actual del evento no permite la operación pedida (patrón State). */
public class TransicionInvalidaException extends EventoException {
    private static final long serialVersionUID = 1L;

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
