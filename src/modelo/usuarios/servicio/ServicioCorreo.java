package modelo.usuarios.servicio;

/**
 * Envío de correos. GestorUsuarios depende de esta interfaz y no de una
 * implementación concreta (SMTP real, bandeja simulada o consola).
 */
public interface ServicioCorreo {

    void enviarRecuperacion(String correo, String codigo);

    /** Indica si los correos se envían de verdad (false = simulados). */
    boolean esReal();
}
