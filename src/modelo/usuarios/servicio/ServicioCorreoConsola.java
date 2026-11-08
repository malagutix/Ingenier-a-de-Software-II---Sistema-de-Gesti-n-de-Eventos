package modelo.usuarios.servicio;

/**
 * Implementación de prueba: en lugar de mandar el mail, lo muestra por consola
 * y guarda el último código para poder simular que el usuario lo copia.
 */
public class ServicioCorreoConsola implements ServicioCorreo {

    private String ultimoCodigo;

    @Override
    public void enviarRecuperacion(String correo, String codigo) {
        this.ultimoCodigo = codigo;
        System.out.println("  [correo a " + correo + "] Tu código de recuperación es: " + codigo);
    }

    @Override
    public boolean esReal() {
        return false;
    }

    public String getUltimoCodigo() {
        return ultimoCodigo;
    }
}
