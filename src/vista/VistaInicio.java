package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;

/**
 * Vista principal después de iniciar sesión: barra superior con el usuario,
 * panel con los permisos del rol y una pestaña por cada área. Qué pestañas
 * se agregan lo decide ControladorInicio según los permisos (patrón Strategy).
 */
public class VistaInicio extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final transient Usuario usuario;
    private final JLabel nombreUsuario = new JLabel();
    private final JTabbedPane pestanas = new JTabbedPane();
    private final JButton cerrar = new JButton("Cerrar sesión");

    public VistaInicio(Usuario usuario) {
        super(new BorderLayout());
        this.usuario = usuario;
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearPanelPermisos(), BorderLayout.WEST);
        JPanel centro = new JPanel(new BorderLayout());
        centro.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        centro.add(pestanas, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private final java.util.Map<Component, Vista> vistas = new java.util.HashMap<>();

    public void agregarPestana(String titulo, Vista vista) {
        vistas.put(vista.componente(), vista);
        pestanas.addTab(titulo, vista.componente());
    }

    public void setTituloPestana(Vista vista, String titulo) {
        int indice = pestanas.indexOfComponent(vista.componente());
        if (indice >= 0) {
            pestanas.setTitleAt(indice, titulo);
        }
    }

    /** Avisa qué pestaña se eligió (para que su controlador recargue los datos). */
    public void alCambiarPestana(Consumer<Vista> accion) {
        pestanas.addChangeListener(e -> accion.accept(vistas.get(pestanas.getSelectedComponent())));
    }

    public void alCerrarSesion(Runnable accion) {
        cerrar.addActionListener(e -> accion.run());
    }

    /** Vuelve a mostrar nombre y rol (por si el usuario modificó sus propios datos). */
    public void actualizarEncabezado() {
        nombreUsuario.setText("<html><b>" + usuario.getNombre() + " " + usuario.getApellido()
                + "</b> &nbsp;·&nbsp; " + usuario.getRol().getNombre() + "</html>");
    }

    @Override
    public Component componente() {
        return this;
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(Ui.FONDO_SUAVE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xD3, 0xD1, 0xC7)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        barra.add(Ui.subtitulo("Gestión de Eventos"), BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        actualizarEncabezado();
        derecha.add(nombreUsuario);
        derecha.add(cerrar);
        barra.add(derecha, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearPanelPermisos() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0xD3, 0xD1, 0xC7)),
                Ui.margen(16)));
        panel.add(Ui.subtitulo("Tus permisos"));
        panel.add(Box.createVerticalStrut(2));
        panel.add(Ui.secundario("Rol " + usuario.getRol().getNombre()));
        panel.add(Box.createVerticalStrut(10));
        for (Permiso permiso : Permiso.values()) {
            JCheckBox check = new JCheckBox(permiso.getDescripcion(), usuario.tienePermiso(permiso));
            check.setEnabled(false);
            check.setOpaque(false);
            panel.add(check);
        }
        return panel;
    }
}
