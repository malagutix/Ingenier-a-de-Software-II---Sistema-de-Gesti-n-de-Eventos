package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Usuario;

/**
 * Vista "PantallaUsuarios" del DCO 3: CDU 004 (buscar y eliminar), CDU 005
 * (asignar roles) y CDU 006 (modificar). Las acciones las resuelve ControladorUsuarios.
 */
public class VistaUsuarios extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;
    private static final String TODOS = "Todos";
    private static final String[] ROLES = {"ADMINISTRADOR", "ORGANIZADOR", "CLIENTE", "PENDIENTE"};

    private final JTextField filtroDni = new JTextField(10);
    private final JTextField filtroCorreo = new JTextField(16);
    private final JComboBox<String> filtroRol = new JComboBox<>(conTodos());

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"ID", "Nombre", "Apellido", "DNI", "Correo electrónico", "Rol"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final transient List<Usuario> filas = new ArrayList<>();

    private final JButton botonBuscar = new JButton("Buscar");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonRol = new JButton("Asignar rol");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JLabel estado = new JLabel(" ");
    private Runnable alBuscar = () -> { };
    private Runnable alModificar = () -> { };

    public VistaUsuarios() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        // Encabezado y filtros
        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.add(Ui.titulo("Gestión de usuarios"), BorderLayout.NORTH);
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("DNI"));
        filtros.add(filtroDni);
        filtros.add(new JLabel("  Correo"));
        filtros.add(filtroCorreo);
        filtros.add(new JLabel("  Rol"));
        filtros.add(filtroRol);
        JButton limpiar = new JButton("Limpiar");
        filtros.add(botonBuscar);
        filtros.add(limpiar);
        arriba.add(filtros, BorderLayout.CENTER);
        add(arriba, BorderLayout.NORTH);

        // Tabla
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Acciones
        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(botonModificar);
        acciones.add(botonRol);
        acciones.add(botonEliminar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        abajo.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        add(abajo, BorderLayout.SOUTH);

        // Eventos propios de la vista
        botonBuscar.addActionListener(e -> alBuscar.run());
        filtroDni.addActionListener(e -> alBuscar.run());
        filtroCorreo.addActionListener(e -> alBuscar.run());
        filtroRol.addActionListener(e -> alBuscar.run());
        limpiar.addActionListener(e -> {
            filtroDni.setText("");
            filtroCorreo.setText("");
            filtroRol.setSelectedItem(TODOS); // dispara la búsqueda
        });
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSeleccionado() != null) {
                    alModificar.run();
                }
            }
        });
        botonModificar.addActionListener(e -> alModificar.run());
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alBuscar(Runnable accion) { this.alBuscar = accion; }
    public void alModificar(Runnable accion) { this.alModificar = accion; }
    public void alAsignarRol(Runnable accion) { botonRol.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    public String getFiltroDni() { return filtroDni.getText(); }
    public String getFiltroCorreo() { return filtroCorreo.getText(); }

    /** Rol elegido en el filtro, o null si es "Todos". */
    public String getFiltroRol() {
        Object rol = filtroRol.getSelectedItem();
        return TODOS.equals(rol) ? null : (String) rol;
    }

    public void mostrarUsuarios(List<Usuario> usuarios) {
        filas.clear();
        modeloTabla.setRowCount(0);
        filas.addAll(usuarios);
        for (Usuario u : filas) {
            modeloTabla.addRow(new Object[] {u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getDni(),
                    u.getCorreoElectronico(), u.getRol().getNombre()});
        }
        actualizarBotones();
    }

    public Usuario getSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : filas.get(tabla.convertRowIndexToModel(fila));
    }

    /** @param exito true = verde, false = rojo, null = gris */
    public void mostrarEstado(String texto, Boolean exito) {
        estado.setText(texto);
        estado.setForeground(exito == null ? Ui.TEXTO_SECUNDARIO : exito ? Ui.EXITO : Ui.ERROR);
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /** CDU 006: formulario con los datos actuales. Se repite hasta que el controlador lo acepte o se cancele. */
    public boolean editarUsuario(Usuario usuario, Predicate<DatosUsuario> alAceptar) {
        FormularioUsuario formulario = new FormularioUsuario(true);
        formulario.cargar(usuario);
        return Ui.dialogoHastaExito(this, formulario, "Modificar usuario #" + usuario.getIdUsuario(),
                formulario::leer, alAceptar);
    }

    /** CDU 005: elegir el rol. Se repite hasta que el controlador lo acepte o se cancele. */
    public boolean elegirRol(Usuario usuario, Predicate<String> alAceptar) {
        JComboBox<String> combo = new JComboBox<>(ROLES);
        combo.setSelectedItem(usuario.getRol().getNombre());
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("Rol para " + usuario.getNombre() + " " + usuario.getApellido() + ":"),
                BorderLayout.NORTH);
        panel.add(combo, BorderLayout.CENTER);
        return Ui.dialogoHastaExito(this, panel, "Asignar rol", () -> (String) combo.getSelectedItem(), alAceptar);
    }

    private void actualizarBotones() {
        boolean haySeleccion = getSeleccionado() != null;
        botonModificar.setEnabled(haySeleccion);
        botonRol.setEnabled(haySeleccion);
        botonEliminar.setEnabled(haySeleccion);
    }

    private static String[] conTodos() {
        String[] opciones = new String[ROLES.length + 1];
        opciones[0] = TODOS;
        System.arraycopy(ROLES, 0, opciones, 1, ROLES.length);
        return opciones;
    }
}
