package modelo.usuarios.excepciones;

/** Excepción base del módulo de usuarios. */
public class UsuarioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsuarioException(String mensaje) {
        super(mensaje);
    }
}
