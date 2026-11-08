package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.DatosLocacion;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Locacion;

/**
 * Vista de gestión de locaciones: alta, modificación, habilitación,
 * eliminación y bloqueo de fechas. Las acciones las resuelve ControladorLocaciones.
 */
public class VistaLocaciones extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    /** Lo que hace el controlador con un bloqueo cargado. Devuelve true si se pudo. */
    public interface AccionBloqueo {
        boolean aplicar(LocalDate desde, LocalDate hasta, String motivo);
    }

    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[] {
            "ID", "Nombre", "Tipo", "Capacidad", "Distancia", "Traslado", "Montaje", "Estado", "Bloqueos"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final transient List<Locacion> filas = new ArrayList<>();

    private final JTextArea detalle = new JTextArea();
    private final DefaultListModel<BloqueoFecha> modeloBloqueos = new DefaultListModel<>();
    private final JList<BloqueoFecha> listaBloqueos = new JList<>(modeloBloqueos);

    private final JButton botonNueva = new JButton("Nueva locación");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonHabilitar = new JButton("Inhabilitar");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JButton botonBloquear = new JButton("Bloquear fechas");
    private final JButton botonDesbloquear = new JButton("Quitar bloqueo");
    private final JLabel estado = new JLabel(" ");
    private Runnable alModificar = () -> { };

    public VistaLocaciones() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Locaciones"), BorderLayout.WEST);
        arriba.add(botonNueva, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        Ui.anchos(tabla, 30, 230, 130, 75, 75, 75, 75, 95, 70);

        JPanel superior = new JPanel(new BorderLayout(0, 6));
        superior.add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new BorderLayout());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(botonModificar);
        botones.add(botonHabilitar);
        botones.add(botonEliminar);
        acciones.add(estado, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        superior.add(acciones, BorderLayout.SOUTH);

        detalle.setEditable(false);
        detalle.setOpaque(false);
        detalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JPanel panelDetalle = new JPanel(new BorderLayout(0, 6));
        panelDetalle.add(Ui.subtitulo("Logística"), BorderLayout.NORTH);
        panelDetalle.add(new JScrollPane(detalle), BorderLayout.CENTER);

        listaBloqueos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel panelBloqueos = new JPanel(new BorderLayout(0, 6));
        panelBloqueos.add(Ui.subtitulo("Fechas bloqueadas"), BorderLayout.NORTH);
        panelBloqueos.add(new JScrollPane(listaBloqueos), BorderLayout.CENTER);
        JPanel botonesBloqueo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botonesBloqueo.add(botonBloquear);
        botonesBloqueo.add(botonDesbloquear);
        panelBloqueos.add(botonesBloqueo, BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new GridLayout(1, 2, 16, 0));
        inferior.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        inferior.add(panelDetalle);
        inferior.add(panelBloqueos);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, inferior);
        division.setResizeWeight(0.55);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        botonModificar.addActionListener(e -> alModificar.run());
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSeleccionada() != null) {
                    alModificar.run();
                }
            }
        });
        listaBloqueos.addListSelectionListener(e -> actualizarBotones());
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alNueva(Runnable accion) { botonNueva.addActionListener(e -> accion.run()); }
    public void alModificar(Runnable accion) { this.alModificar = accion; }
    public void alCambiarHabilitacion(Runnable accion) { botonHabilitar.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }
    public void alBloquear(Runnable accion) { botonBloquear.addActionListener(e -> accion.run()); }
    public void alDesbloquear(Runnable accion) { botonDesbloquear.addActionListener(e -> accion.run()); }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    /** Muestra las locaciones manteniendo la seleccionada (o la primera). */
    public void mostrarLocaciones(List<Locacion> locaciones) {
        Locacion anterior = getSeleccionada();
        filas.clear();
        modeloTabla.setRowCount(0);
        filas.addAll(locaciones);
        for (Locacion l : filas) {
            modeloTabla.addRow(new Object[] {l.getIdLocacion(), l.getNombre(), l.getTipo(), l.getCapacidad(),
                    km(l.getDistanciaKm()), Formato.minutos(l.getTiempoTrasladoMin()),
                    Formato.minutos(l.getTiempoMontajeMin()), l.isHabilitada() ? "Habilitada" : "Inhabilitada",
                    l.getBloqueos().size()});
        }
        int indice = anterior == null ? -1 : filas.indexOf(anterior);
        if (indice < 0 && !filas.isEmpty()) {
            indice = 0;
        }
        if (indice >= 0) {
            tabla.setRowSelectionInterval(indice, indice);
        }
        mostrarDetalle();
    }

    public void seleccionar(Locacion l) {
        int indice = filas.indexOf(l);
        if (indice >= 0) {
            tabla.setRowSelectionInterval(indice, indice);
        }
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar la cantidad */
    public void mostrarEstado(String mensajeExito) {
        estado.setForeground(mensajeExito != null ? Ui.EXITO : Ui.TEXTO_SECUNDARIO);
        estado.setText(mensajeExito != null ? mensajeExito : filas.size() + " locación(es)");
    }

    public Locacion getSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 || fila >= filas.size() ? null : filas.get(fila);
    }

    public BloqueoFecha getBloqueoSeleccionado() {
        return listaBloqueos.getSelectedValue();
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /**
     * Formulario de locación. Se repite hasta que el controlador lo acepte o se cancele.
     *
     * @param actual la locación a modificar, o null si es nueva
     */
    public boolean editarLocacion(String titulo, Locacion actual, Predicate<DatosLocacion> alAceptar) {
        FormularioLocacion formulario = new FormularioLocacion();
        if (actual != null) {
            formulario.cargar(actual);
        }
        return Ui.dialogoHastaExito(this, formulario, titulo, formulario::leer, alAceptar);
    }

    /** Pide el rango de fechas y el motivo. Se repite hasta que el controlador lo acepte o se cancele. */
    public boolean pedirBloqueo(Locacion l, AccionBloqueo alAceptar) {
        JSpinner desde = Ui.selectorFecha(LocalDate.now().plusDays(1));
        JSpinner hasta = Ui.selectorFecha(LocalDate.now().plusDays(1));
        JTextField motivo = new JTextField(24);
        JPanel formulario = new JPanel(new GridBagLayout());
        Ui.fila(formulario, 0, "Desde", desde);
        Ui.fila(formulario, 1, "Hasta", hasta);
        Ui.fila(formulario, 2, "Motivo", motivo);
        return Ui.dialogoHastaExito(this, formulario, "Bloquear fechas de " + l.getNombre(), () -> Boolean.TRUE,
                ok -> alAceptar.aplicar(Ui.getFecha(desde), Ui.getFecha(hasta), motivo.getText()));
    }

    private void mostrarDetalle() {
        Locacion l = getSeleccionada();
        modeloBloqueos.clear();
        if (l == null) {
            detalle.setText("");
            actualizarBotones();
            return;
        }
        detalle.setText(String.format(
                "%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s%n%-22s %s",
                "Dirección:", l.getDireccion(),
                "Tipo:", l.getTipo(),
                "Vehículos:", l.getVehiculosRequeridos(),
                "Cámara de frío:", l.isRequiereCamaraFrio() ? "Sí" : "No",
                "Tiempo de montaje:", Formato.minutos(l.getTiempoMontajeMin()),
                "Otros requerimientos:", l.getOtrosRequerimientos().isEmpty() ? "-" : l.getOtrosRequerimientos()));
        detalle.setCaretPosition(0);
        for (BloqueoFecha b : l.getBloqueos()) {
            modeloBloqueos.addElement(b);
        }
        botonHabilitar.setText(l.isHabilitada() ? "Inhabilitar" : "Habilitar");
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean hay = getSeleccionada() != null;
        botonModificar.setEnabled(hay);
        botonHabilitar.setEnabled(hay);
        botonEliminar.setEnabled(hay);
        botonBloquear.setEnabled(hay);
        botonDesbloquear.setEnabled(hay && listaBloqueos.getSelectedValue() != null);
    }

    private static String km(double km) {
        return (km == Math.floor(km) ? String.valueOf((long) km) : String.valueOf(km)).replace('.', ',') + " km";
    }
}
