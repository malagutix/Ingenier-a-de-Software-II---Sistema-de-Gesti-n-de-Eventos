package modelo.usuarios.excepciones;

/** Error al iniciar sesión o al restablecer la contraseña (RF002, RF003). */
public class AutenticacionException extends UsuarioException {
    private static final long serialVersionUID = 1L;

    public AutenticacionException(String mensaje) {
        super(mensaje);
    }
}
