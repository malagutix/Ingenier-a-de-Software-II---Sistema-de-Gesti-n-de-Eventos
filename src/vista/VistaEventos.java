package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.DatosEvento;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.HojaServicio;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.entidades.estado.TransicionEvento;

/**
 * Vista de eventos del organizador: CDU 011 (crear), CDU 012 (eliminar),
 * CDU 013 (validar), CDU 014 (modificar) y CDU 015 (hoja de servicio).
 * Las acciones las resuelve ControladorEventos.
 */
public class VistaEventos extends JPanel implements VistaConPlanificacion {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[] {
            "ID", "Fecha", "Horario", "Tipo", "Planificación", "Ubicación", "Asist.", "Estado", "Cliente"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new TablaSeleccionMultiple(modeloTabla);
    private final transient List<Evento> filas = new ArrayList<>();

    private final JButton botonPlanificar = new JButton("Planificar eventos");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonValidar = new JButton("Validar / cambiar estado");
    private final JButton botonHoja = new JButton("Hoja de servicio");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JLabel estado = new JLabel(" ");
    private Runnable alModificar = () -> { };
    private boolean puedeValidar;
    private boolean puedeGenerarHoja;

    public VistaEventos() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Eventos"), BorderLayout.WEST);
        arriba.add(Ui.secundario("Hacé clic en uno o varios eventos para seleccionarlos. Doble clic para modificar."),
                BorderLayout.SOUTH);
        arriba.add(botonPlanificar, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        Ui.anchos(tabla, 28, 85, 142, 95, 150, 135, 40, 165, 115);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(botonModificar);
        acciones.add(botonValidar);
        acciones.add(botonHoja);
        acciones.add(botonEliminar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        botonModificar.addActionListener(e -> alModificar.run());
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && getSeleccionado() != null) {
                    alModificar.run();
                }
            }
        });
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alPlanificar(Runnable accion) { botonPlanificar.addActionListener(e -> accion.run()); }
    public void alModificar(Runnable accion) { this.alModificar = accion; }
    public void alValidar(Runnable accion) { botonValidar.addActionListener(e -> accion.run()); }
    public void alGenerarHoja(Runnable accion) { botonHoja.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    /** Validar y generar la hoja dependen de los permisos del rol. */
    public void setPermisos(boolean puedeValidar, boolean puedeGenerarHoja) {
        this.puedeValidar = puedeValidar;
        this.puedeGenerarHoja = puedeGenerarHoja;
        actualizarBotones();
    }

    /** Muestra los eventos manteniendo los seleccionados, si siguen estando. */
    public void mostrarEventos(List<Evento> eventos) {
        List<Evento> anteriores = getSeleccionados();
        filas.clear();
        modeloTabla.setRowCount(0);
        filas.addAll(eventos);
        for (Evento e : filas) {
            modeloTabla.addRow(new Object[] {e.getIdEvento(), Formato.fecha(e.getFecha()), e.getHorarioCorto(), e.getTipo(),
                    "(" + e.getPlanificacion().posicionDe(e).replace(" de ", "/") + ") "
                            + e.getPlanificacion().getNombre(),
                    e.getLocacion().getNombre(), e.getCantidadAsistentes(),
                    e.getEstado().getNombre(), e.getCliente().getNombre() + " " + e.getCliente().getApellido()});
        }
        for (Evento anterior : anteriores) {
            int indice = filas.indexOf(anterior);
            if (indice >= 0) {
                tabla.addRowSelectionInterval(indice, indice);
            }
        }
        actualizarBotones();
    }

    public void seleccionar(Evento evento) {
        int indice = filas.indexOf(evento);
        if (indice >= 0) {
            tabla.setRowSelectionInterval(indice, indice);
        }
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar la cantidad */
    public void mostrarEstado(String mensajeExito) {
        estado.setForeground(mensajeExito != null ? Ui.EXITO : Ui.TEXTO_SECUNDARIO);
        estado.setText(mensajeExito != null ? mensajeExito : filas.size() + " evento(s)");
    }

    /** El evento seleccionado, o null si no hay ninguno o hay más de uno. */
    public Evento getSeleccionado() {
        List<Evento> seleccionados = getSeleccionados();
        return seleccionados.size() == 1 ? seleccionados.get(0) : null;
    }

    /** Todos los eventos seleccionados, en el orden de la tabla. */
    public List<Evento> getSeleccionados() {
        List<Evento> seleccionados = new ArrayList<>();
        for (int fila : tabla.getSelectedRows()) {
            int indice = tabla.convertRowIndexToModel(fila);
            if (indice < filas.size()) {
                seleccionados.add(filas.get(indice));
            }
        }
        return seleccionados;
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /** CDU 014: formulario del evento. Se repite hasta que el controlador lo acepte o se cancele. */
    public boolean editarEvento(Evento evento, List<TipoEvento> tipos, List<Locacion> locaciones,
                                Predicate<DatosEvento> alAceptar) {
        FormularioEvento formulario = new FormularioEvento(evento, tipos, locaciones);
        return Ui.dialogoHastaExito(this, formulario, "Modificar evento #" + evento.getIdEvento(),
                formulario::leer, alAceptar);
    }

    /** CDU 013: elegir el nuevo estado entre los posibles. Devuelve null si se cerró. */
    public TransicionEvento elegirTransicion(Evento e, Set<TransicionEvento> posibles) {
        List<Object> opciones = new ArrayList<>(posibles);
        opciones.add("Cerrar");
        int elegida = JOptionPane.showOptionDialog(this,
                "<html>Evento #" + e.getIdEvento() + " - " + e.getTipo() + " - " + Formato.fecha(e.getFecha())
                        + "<br><br>Estado actual: <b>" + e.getEstado().getNombre() + "</b>"
                        + "<br>Elegí el nuevo estado:</html>",
                "Validar evento", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                opciones.toArray(), opciones.get(0));
        if (elegida < 0 || elegida >= posibles.size()) {
            return null;
        }
        return (TransicionEvento) opciones.get(elegida);
    }

    /** Pide la nueva fecha al reprogramar. Devuelve null si se canceló. */
    public LocalDate pedirNuevaFecha(LocalDate sugerida) {
        JSpinner fecha = Ui.selectorFecha(sugerida);
        JPanel panel = new JPanel(new GridBagLayout());
        Ui.fila(panel, 0, "Nueva fecha", fecha);
        if (JOptionPane.showConfirmDialog(this, panel, "Reprogramar evento", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return null;
        }
        return Ui.getFecha(fecha);
    }

    /**
     * CDU 015: muestra la hoja de servicio con un botón por formato.
     *
     * @return el índice del formato elegido para exportar, o -1 si se cerró
     */
    public int mostrarHoja(HojaServicio hoja, List<String> formatos) {
        JTextArea texto = new JTextArea(hoja.getDetalle());
        texto.setEditable(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        texto.setRows(22);
        texto.setColumns(78);
        String[] opciones = new String[formatos.size() + 1];
        for (int i = 0; i < formatos.size(); i++) {
            opciones[i] = "Exportar " + formatos.get(i);
        }
        opciones[formatos.size()] = "Cerrar";
        int elegida = JOptionPane.showOptionDialog(this, new JScrollPane(texto), hoja.getTitulo(),
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
        return elegida >= 0 && elegida < formatos.size() ? elegida : -1;
    }

    /** Pide dónde guardar el archivo (le agrega la extensión si falta). Devuelve null si se canceló. */
    public File elegirArchivo(String nombreSugerido, String extension) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(nombreSugerido));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith("." + extension)) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + "." + extension);
        }
        return archivo;
    }

    /** Avisa que se exportó y, si el usuario quiere, abre el archivo. */
    public void ofrecerAbrir(File archivo) {
        int abrir = JOptionPane.showConfirmDialog(this, "Hoja exportada en:\n" + archivo.getAbsolutePath()
                + "\n\n¿Querés abrirla?", "Exportación exitosa", JOptionPane.YES_NO_OPTION);
        if (abrir == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(archivo);
            } catch (IOException | UnsupportedOperationException ex) {
                Ui.error(this, "No se pudo abrir el archivo automáticamente. Abrilo desde:\n" + archivo.getAbsolutePath());
            }
        }
    }

    private void actualizarBotones() {
        // Con uno solo seleccionado están todas las acciones; con varios, solo Eliminar (en lote)
        boolean uno = getSeleccionado() != null;
        botonModificar.setEnabled(uno);
        botonEliminar.setEnabled(tabla.getSelectedRowCount() > 0);
        botonValidar.setEnabled(uno && puedeValidar);
        botonHoja.setEnabled(uno && puedeGenerarHoja);
    }
}
