package modelo.eventos.excepciones;

/** El evento, la solicitud o la locación buscada no existe. */
public class NoEncontradoException extends EventoException {
    private static final long serialVersionUID = 1L;

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
