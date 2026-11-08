package vista;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Diálogos del arranque de la aplicación, antes de que exista la ventana
 * principal: problemas con la configuración de correo o de la base de datos.
 */
public class VistaArranque implements Vista {

    /** Qué hacer si no se pudo conectar con la base de datos. */
    public enum OpcionConexion { REINTENTAR, MODO_DEMOSTRACION, SALIR }

    @Override
    public Component componente() {
        return null;
    }

    public void advertir(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    /** CDU 007, alternativa 7.1: sin conexión no se continúa. Ofrece reintentar, modo demostración o salir. */
    public OpcionConexion preguntarSinConexion(String problema, boolean permiteReintentar) {
        String[] opciones = permiteReintentar
                ? new String[] {"Reintentar", "Usar modo demostración", "Salir"}
                : new String[] {"Usar modo demostración", "Salir"};
        int opcion = JOptionPane.showOptionDialog(null, problema
                        + "\n\nRevisá los datos de basedatos.properties (servidor, puerto, usuario y contraseña)."
                        + "\nEn modo demostración los datos se guardan solo en memoria.",
                "No se pudo conectar con la base de datos", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, opciones, opciones[0]);
        if (opcion == JOptionPane.CLOSED_OPTION || opcion == opciones.length - 1) {
            return OpcionConexion.SALIR;
        }
        return permiteReintentar && opcion == 0 ? OpcionConexion.REINTENTAR : OpcionConexion.MODO_DEMOSTRACION;
    }
}
