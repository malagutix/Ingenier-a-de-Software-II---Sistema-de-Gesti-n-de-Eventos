package modelo.usuarios.excepciones;

/** El usuario no tiene el permiso necesario para la operación (RNF001). */
public class AccesoDenegadoException extends UsuarioException {
    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
