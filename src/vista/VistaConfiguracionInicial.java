package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import modelo.usuarios.entidades.DatosUsuario;

/**
 * Vista de la primera ejecución con la base de datos vacía: se crea el
 * usuario administrador (requerimiento de implantación) y, si se quiere, se
 * cargan datos de ejemplo. La acción la resuelve ControladorConfiguracionInicial.
 */
public class VistaConfiguracionInicial extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final FormularioUsuario formulario = new FormularioUsuario(false);
    private final JCheckBox ejemplos = new JCheckBox("Cargar datos de ejemplo para probar el sistema", true);
    private final JButton crear = new JButton("Crear administrador");
    private final JLabel estado = new JLabel(" ");

    public VistaConfiguracionInicial() {
        super(new GridBagLayout());
        setBackground(Ui.FONDO_SUAVE);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(java.awt.Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(0xD3, 0xD1, 0xC7)), Ui.margen(24)));
        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(Ui.titulo("Configuración inicial"));
        encabezado.add(Box.createVerticalStrut(4));
        encabezado.add(Ui.secundario("<html>La base de datos está lista y todavía no tiene usuarios.<br>"
                + "Creá el usuario administrador para empezar a usar el sistema.</html>"));
        tarjeta.add(encabezado, BorderLayout.NORTH);
        formulario.setOpaque(false);
        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 8));
        abajo.setOpaque(false);
        ejemplos.setOpaque(false);
        JPanel opciones = new JPanel();
        opciones.setOpaque(false);
        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
        opciones.add(ejemplos);
        opciones.add(Ui.secundario("    Agrega organizadora, clientes, tipos de evento, locaciones, solicitudes y eventos."));
        abajo.add(opciones, BorderLayout.NORTH);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.setOpaque(false);
        botones.add(crear);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(botones, BorderLayout.EAST);
        abajo.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        tarjeta.add(abajo, BorderLayout.SOUTH);
        add(tarjeta);
    }

    public void alCrear(Runnable accion) {
        crear.addActionListener(e -> accion.run());
    }

    /** Datos del administrador, o null si las contraseñas no coinciden (ya se avisó). */
    public DatosUsuario leerDatos() {
        return formulario.leer();
    }

    public boolean isCargarEjemplos() {
        return ejemplos.isSelected();
    }

    /** Mientras se crea: deshabilita el botón, muestra el cursor de espera y un mensaje. */
    public void mostrarTrabajando(String mensaje) {
        crear.setEnabled(false);
        ocupado(true);
        estado.setForeground(Ui.TEXTO_SECUNDARIO);
        estado.setText(mensaje);
    }

    public void terminarTrabajo(boolean limpiarMensaje) {
        if (limpiarMensaje) {
            estado.setText(" ");
        }
        crear.setEnabled(true);
        ocupado(false);
    }

    public JButton getBotonCrear() {
        return crear;
    }

    @Override
    public Component componente() {
        return this;
    }
}
