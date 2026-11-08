package modelo.usuarios.entidades;

/**
 * Datos que se ingresan en el formulario de registro (RF001) o de
 * modificación (RF006). En una modificación, los campos en null no se cambian.
 */
public final class DatosUsuario {

    private final String nombre;
    private final String apellido;
    private final String dni;
    private final String correoElectronico;
    private final String contrasena;

    public DatosUsuario(String nombre, String apellido, String dni, String correoElectronico, String contrasena) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correoElectronico = correoElectronico;
        this.contrasena = contrasena;
    }

    public String nombre() { return nombre; }
    public String apellido() { return apellido; }
    public String dni() { return dni; }
    public String correoElectronico() { return correoElectronico; }
    public String contrasena() { return contrasena; }
}
