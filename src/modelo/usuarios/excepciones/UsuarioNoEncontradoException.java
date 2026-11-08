package modelo.usuarios.excepciones;

/** El usuario buscado no existe (RF004, RF005, RF006). */
public class UsuarioNoEncontradoException extends UsuarioException {
    private static final long serialVersionUID = 1L;

    public UsuarioNoEncontradoException() {
        super("Usuario no encontrado");
    }
}
