package modelo.usuarios.excepciones;

/** Datos incompletos, con formato incorrecto o duplicados (RF001, RF006). */
public class DatosInvalidosException extends UsuarioException {
    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
