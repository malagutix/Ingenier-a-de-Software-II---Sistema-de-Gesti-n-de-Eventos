package modelo.usuarios.servicio;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.usuarios.entidades.CorreoSimulado;

/**
 * Implementación de ServicioCorreo cuando no hay un servidor de correo
 * configurado: guarda los mails en una bandeja que se abre desde el login.
 */
public class BandejaCorreoSimulada implements ServicioCorreo {

    /** Un correo enviado por el sistema. */
    private final List<CorreoSimulado> correos = new ArrayList<>();

    @Override
    public void enviarRecuperacion(String correo, String codigo) {
        correos.add(new CorreoSimulado(correo, codigo, LocalTime.now()));
    }

    @Override
    public boolean esReal() {
        return false;
    }

    public List<CorreoSimulado> getCorreos() {
        List<CorreoSimulado> copia = new ArrayList<>(correos);
        Collections.reverse(copia); // el más nuevo primero
        return copia;
    }

    public CorreoSimulado getUltimo() {
        return correos.isEmpty() ? null : correos.get(correos.size() - 1);
    }
}
