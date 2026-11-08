package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.Formato;
import modelo.persistencia.ConsultaTabla;
import modelo.persistencia.EstadoBaseDatos;

/**
 * Vista del CDU 007 - Gestionar base de datos: conexión, tablas con su
 * cantidad de registros, consulta de registros, respaldo, restauración y
 * verificación de integridad. Las acciones las resuelve ControladorBaseDatos.
 */
public class VistaBaseDatos extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final JTextArea datosConexion = new JTextArea();
    private final DefaultTableModel modeloTablas = new DefaultTableModel(new String[] {"Tabla", "Registros"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tablas = new JTable(modeloTablas);
    private final JTextArea resultado = new JTextArea();
    private final JButton botonConsultar = new JButton("Ver registros");
    private final JButton botonRespaldar = new JButton("Respaldar ahora");
    private final JButton botonRestaurar = new JButton("Restaurar...");
    private final JButton botonVerificar = new JButton("Verificar integridad");
    private Runnable alVerRegistros = () -> { };

    /**
     * @param conBaseDeDatos false si el sistema funciona en modo demostración (en memoria)
     * @param motivoSinBase  por qué funciona en modo demostración, o null
     */
    public VistaBaseDatos(boolean conBaseDeDatos, String motivoSinBase) {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Base de datos"), BorderLayout.NORTH);
        arriba.add(Ui.secundario("Las altas, bajas y modificaciones de datos se hacen desde cada pestaña, "
                + "que valida las reglas antes de guardar."), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        if (!conBaseDeDatos) {
            JTextArea aviso = new JTextArea("El sistema está funcionando en modo demostración: los datos se guardan "
                    + "solo en memoria y se pierden al cerrar.\n\nPara usar PostgreSQL, completá el archivo "
                    + "basedatos.properties (en la carpeta del proyecto) con los datos de tu servidor y "
                    + "habilitado=true, y volvé a abrir la aplicación."
                    + (motivoSinBase == null ? "" : "\n\nMotivo: " + motivoSinBase));
            aviso.setEditable(false);
            aviso.setOpaque(false);
            aviso.setLineWrap(true);
            aviso.setWrapStyleWord(true);
            add(aviso, BorderLayout.CENTER);
            return;
        }

        datosConexion.setEditable(false);
        datosConexion.setOpaque(false);
        datosConexion.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel conexion = new JPanel(new BorderLayout(0, 6));
        conexion.add(Ui.subtitulo("Conexión"), BorderLayout.NORTH);
        conexion.add(datosConexion, BorderLayout.CENTER);
        JPanel accionesGenerales = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        accionesGenerales.add(botonRespaldar);
        accionesGenerales.add(botonRestaurar);
        accionesGenerales.add(botonVerificar);
        accionesGenerales.setBorder(BorderFactory.createEmptyBorder(4, -8, 0, 0));
        conexion.add(accionesGenerales, BorderLayout.SOUTH);

        tablas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablas.setRowHeight(24);
        tablas.setFillsViewportHeight(true);
        JPanel panelTablas = new JPanel(new BorderLayout(0, 6));
        panelTablas.add(Ui.subtitulo("Tablas"), BorderLayout.NORTH);
        panelTablas.add(new JScrollPane(tablas), BorderLayout.CENTER);
        JPanel botonesTablas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botonesTablas.add(botonConsultar);
        panelTablas.add(botonesTablas, BorderLayout.SOUTH);

        resultado.setEditable(false);
        resultado.setLineWrap(true);
        resultado.setWrapStyleWord(true);
        JPanel panelResultado = new JPanel(new BorderLayout(0, 6));
        panelResultado.add(Ui.subtitulo("Resultado"), BorderLayout.NORTH);
        panelResultado.add(new JScrollPane(resultado), BorderLayout.CENTER);

        JPanel izquierda = new JPanel(new BorderLayout(0, 16));
        izquierda.add(conexion, BorderLayout.NORTH);
        izquierda.add(panelResultado, BorderLayout.CENTER);
        JPanel centro = new JPanel(new GridLayout(1, 2, 16, 0));
        centro.add(izquierda);
        centro.add(panelTablas);
        add(centro, BorderLayout.CENTER);

        botonConsultar.addActionListener(e -> alVerRegistros.run());
        tablas.getSelectionModel().addListSelectionListener(e -> botonConsultar.setEnabled(tablas.getSelectedRow() >= 0));
        tablas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && tablas.getSelectedRow() >= 0) {
                    alVerRegistros.run();
                }
            }
        });
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alRespaldar(Runnable accion) { botonRespaldar.addActionListener(e -> accion.run()); }
    public void alRestaurar(Runnable accion) { botonRestaurar.addActionListener(e -> accion.run()); }
    public void alVerificar(Runnable accion) { botonVerificar.addActionListener(e -> accion.run()); }
    public void alVerRegistros(Runnable accion) { this.alVerRegistros = accion; }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    public void mostrarEstado(EstadoBaseDatos e) {
        datosConexion.setText(String.format(
                "%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s",
                "Servidor:", e.getServidor(),
                "Base de datos:", e.getBase() + (e.isBaseCreadaAlIniciar() ? "  (creada en este inicio)" : ""),
                "Usuario:", e.getUsuario(),
                "Versión de PostgreSQL:", e.getVersionServidor(),
                "Último respaldo:", e.getUltimoRespaldo() == null ? "nunca" : Formato.fechaHora(e.getUltimoRespaldo()),
                "Carpeta de respaldos:", e.getCarpetaRespaldos()));
        datosConexion.setToolTipText("Respaldos automáticos en " + e.getCarpetaRespaldos());
        int seleccion = tablas.getSelectedRow();
        modeloTablas.setRowCount(0);
        for (Map.Entry<String, Long> t : e.getRegistros().entrySet()) {
            modeloTablas.addRow(new Object[] {t.getKey(), t.getValue()});
        }
        if (seleccion >= 0 && seleccion < modeloTablas.getRowCount()) {
            tablas.setRowSelectionInterval(seleccion, seleccion);
        }
        botonConsultar.setEnabled(tablas.getSelectedRow() >= 0);
    }

    /** Nombre de la tabla seleccionada, o null. */
    public String getTablaSeleccionada() {
        int fila = tablas.getSelectedRow();
        return fila < 0 ? null : (String) modeloTablas.getValueAt(fila, 0);
    }

    public void mostrarResultado(String texto) {
        resultado.setText(texto);
        resultado.setCaretPosition(0);
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /** Muestra los registros de una tabla en una ventana aparte (solo lectura). */
    public void mostrarRegistros(String tabla, ConsultaTabla q) {
        DefaultTableModel modeloTabla = new DefaultTableModel(q.getColumnas().toArray(), 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
        for (Object[] f : q.getFilas()) {
            modeloTabla.addRow(f);
        }
        JTable vista = new JTable(modeloTabla);
        vista.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < vista.getColumnCount(); i++) {
            vista.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 50 : 140);
        }
        JPanel contenido = new JPanel(new BorderLayout(0, 6));
        contenido.setBorder(Ui.margen(12));
        contenido.add(new JLabel(q.getFilas().size() + " registro(s)"
                + (q.isRecortada() ? " (se muestran los primeros " + q.getFilas().size() + ")" : "")
                + ". Solo lectura; los datos sensibles se ocultan."), BorderLayout.NORTH);
        contenido.add(new JScrollPane(vista), BorderLayout.CENTER);
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Tabla " + tabla);
        dialogo.setModal(true);
        dialogo.setContentPane(contenido);
        dialogo.setSize(900, 480);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    /** Dónde guardar el respaldo, o null si se canceló. */
    public Path elegirDestinoRespaldo() {
        JFileChooser elegir = selectorArchivos();
        elegir.setSelectedFile(new File("respaldo-eventos.txt"));
        return elegir.showSaveDialog(this) == JFileChooser.APPROVE_OPTION ? elegir.getSelectedFile().toPath() : null;
    }

    /** Qué respaldo restaurar, o null si se canceló. */
    public Path elegirRespaldoARestaurar() {
        JFileChooser elegir = selectorArchivos();
        return elegir.showOpenDialog(this) == JFileChooser.APPROVE_OPTION ? elegir.getSelectedFile().toPath() : null;
    }

    private static JFileChooser selectorArchivos() {
        JFileChooser elegir = new JFileChooser(new File("respaldos").getAbsoluteFile());
        elegir.setFileFilter(new FileNameExtensionFilter("Respaldos (*.txt)", "txt"));
        elegir.setBorder(BorderFactory.createEmptyBorder());
        return elegir;
    }
}
