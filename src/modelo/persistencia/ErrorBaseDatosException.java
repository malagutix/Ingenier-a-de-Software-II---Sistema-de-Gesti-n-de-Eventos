package modelo.persistencia;

/** Error al comunicarse con la base de datos (conexión caída, credenciales, etc.). CDU 007, alternativa 7.1. */
public class ErrorBaseDatosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErrorBaseDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ErrorBaseDatosException(String mensaje) {
        super(mensaje);
    }
}
