package modelo.eventos.excepciones;

/** Datos incompletos o fuera de rango en una solicitud o un evento. */
public class DatosEventoInvalidosException extends EventoException {
    private static final long serialVersionUID = 1L;

    public DatosEventoInvalidosException(String mensaje) {
        super(mensaje);
    }
}
