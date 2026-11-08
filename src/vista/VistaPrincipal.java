package vista;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Ventana de la aplicación. Muestra una pantalla por vez (configuración
 * inicial, login o inicio); cuál mostrar lo decide ControladorPrincipal.
 */
public class VistaPrincipal extends JFrame implements Vista {

    private static final long serialVersionUID = 1L;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);

    public VistaPrincipal() {
        super("Sistema de Gestión de Eventos");
        setContentPane(contenedor);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 640));
        setSize(1240, 740);
        setLocationRelativeTo(null);
    }

    /** Aspecto visual del sistema operativo (o Nimbus). Se llama antes de crear las vistas. */
    public static void configurarAspecto() {
        Ui.configurarAspecto();
    }

    public void mostrarConfiguracion(VistaConfiguracionInicial vista) {
        mostrar(vista, "configuracion", vista.getBotonCrear());
    }

    public void mostrarLogin(VistaLogin vista) {
        mostrar(vista, "login", vista.getBotonIngresar());
    }

    public void mostrarInicio(VistaInicio vista) {
        mostrar(vista, "inicio", null);
    }

    /** Reemplaza la pantalla que tenga ese nombre y la muestra. */
    private void mostrar(Component pantalla, String nombre, JButton botonPorDefecto) {
        for (Component c : contenedor.getComponents()) {
            if (nombre.equals(c.getName()) && c != pantalla) {
                contenedor.remove(c);
            }
        }
        if (pantalla.getParent() != contenedor) {
            pantalla.setName(nombre);
            contenedor.add(pantalla, nombre);
        }
        getRootPane().setDefaultButton(botonPorDefecto);
        tarjetas.show(contenedor, nombre);
    }

    @Override
    public Component componente() {
        return this;
    }
}
