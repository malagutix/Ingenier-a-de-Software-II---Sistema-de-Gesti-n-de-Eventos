package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Planificacion;

/**
 * Vista de la planificación de múltiples eventos: arriba las planificaciones
 * generales y abajo los eventos de la seleccionada. Las acciones (planificar,
 * agregar eventos, renombrar, reprogramar o cancelar todo) las resuelve
 * ControladorPlanificaciones.
 */
public class VistaPlanificaciones extends JPanel implements VistaConPlanificacion {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloPlanificaciones = tablaModelo(
            "ID", "Nombre", "Cliente", "Solicitud", "Desde", "Hasta", "Eventos", "Estado general");
    private final JTable tablaPlanificaciones = new JTable(modeloPlanificaciones);
    private final transient List<Planificacion> filas = new ArrayList<>();

    private final DefaultTableModel modeloEventos = tablaModelo(
            "ID", "Fecha", "Horario", "Tipo", "Ubicación", "Asistentes", "Estado");
    private final JTable tablaEventos = new JTable(modeloEventos);
    private final JLabel tituloEventos = Ui.subtitulo("Eventos de la planificación");

    private final JButton botonPlanificar = new JButton("Planificar eventos");
    private final JButton botonAgregar = new JButton("Agregar eventos");
    private final JButton botonRenombrar = new JButton("Renombrar");
    private final JButton botonReprogramar = new JButton("Reprogramar todo");
    private final JButton botonCancelar = new JButton("Cancelar todo");
    private final JLabel estado = new JLabel(" ");
    private boolean puedeValidar;

    public VistaPlanificaciones() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Planificaciones"), BorderLayout.WEST);
        arriba.add(botonPlanificar, BorderLayout.EAST);
        arriba.add(Ui.secundario("Cada planificación agrupa los eventos generados desde una misma solicitud "
                + "(por ejemplo, un congreso de varios días)."), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        configurar(tablaPlanificaciones);
        Ui.anchos(tablaPlanificaciones, 30, 210, 120, 65, 85, 85, 60, 110);
        configurar(tablaEventos);
        Ui.anchos(tablaEventos, 30, 90, 140, 120, 220, 80, 160);
        tablaEventos.setRowSelectionAllowed(false);

        JPanel superior = new JPanel(new BorderLayout(0, 6));
        superior.add(new JScrollPane(tablaPlanificaciones), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new BorderLayout());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(botonAgregar);
        botones.add(botonRenombrar);
        botones.add(botonReprogramar);
        botones.add(botonCancelar);
        acciones.add(estado, BorderLayout.WEST);
        acciones.add(botones, BorderLayout.EAST);
        superior.add(acciones, BorderLayout.SOUTH);

        JPanel inferior = new JPanel(new BorderLayout(0, 6));
        inferior.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        inferior.add(tituloEventos, BorderLayout.NORTH);
        inferior.add(new JScrollPane(tablaEventos), BorderLayout.CENTER);
        inferior.add(Ui.secundario("Para modificar, validar o generar la hoja de servicio de un evento, "
                + "usá la pestaña Eventos."), BorderLayout.SOUTH);

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, inferior);
        division.setResizeWeight(0.5);
        division.setBorder(null);
        add(division, BorderLayout.CENTER);

        tablaPlanificaciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarEventos();
                actualizarBotones();
            }
        });
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alPlanificar(Runnable accion) { botonPlanificar.addActionListener(e -> accion.run()); }
    public void alAgregarEventos(Runnable accion) { botonAgregar.addActionListener(e -> accion.run()); }
    public void alRenombrar(Runnable accion) { botonRenombrar.addActionListener(e -> accion.run()); }
    public void alReprogramar(Runnable accion) { botonReprogramar.addActionListener(e -> accion.run()); }
    public void alCancelar(Runnable accion) { botonCancelar.addActionListener(e -> accion.run()); }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    /** Reprogramar y cancelar en bloque requieren el permiso de validar eventos. */
    public void setPuedeValidar(boolean puedeValidar) {
        this.puedeValidar = puedeValidar;
        actualizarBotones();
    }

    /** Muestra las planificaciones manteniendo la seleccionada (o la primera). */
    public void mostrarPlanificaciones(List<Planificacion> planificaciones) {
        Planificacion anterior = getSeleccionada();
        filas.clear();
        modeloPlanificaciones.setRowCount(0);
        filas.addAll(planificaciones);
        for (Planificacion p : filas) {
            modeloPlanificaciones.addRow(new Object[] {p.getIdPlanificacion(), p.getNombre(),
                    p.getCliente().getNombre() + " " + p.getCliente().getApellido(),
                    "#" + p.getSolicitud().getIdSolicitud(), Formato.fecha(p.getFechaInicio()),
                    Formato.fecha(p.getFechaFin()), p.getCantidadEventos(), p.getEstadoGeneral()});
        }
        int indice = anterior == null ? -1 : filas.indexOf(anterior);
        if (indice < 0 && !filas.isEmpty()) {
            indice = 0;
        }
        if (indice >= 0) {
            tablaPlanificaciones.setRowSelectionInterval(indice, indice);
        }
        mostrarEventos();
        actualizarBotones();
    }

    public void seleccionar(Planificacion p) {
        int indice = filas.indexOf(p);
        if (indice >= 0) {
            tablaPlanificaciones.setRowSelectionInterval(indice, indice);
        }
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar la cantidad */
    public void mostrarEstado(String mensajeExito) {
        estado.setForeground(mensajeExito != null ? Ui.EXITO : Ui.TEXTO_SECUNDARIO);
        estado.setText(mensajeExito != null ? mensajeExito : filas.size() + " planificación(es)");
    }

    public Planificacion getSeleccionada() {
        int fila = tablaPlanificaciones.getSelectedRow();
        return fila < 0 || fila >= filas.size() ? null : filas.get(fila);
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /** Devuelve el nombre escrito, o null si se canceló. */
    public String pedirNombre(String actual) {
        return (String) JOptionPane.showInputDialog(this, "Nuevo nombre de la planificación:",
                "Renombrar planificación", JOptionPane.PLAIN_MESSAGE, null, null, actual);
    }

    /** Devuelve la nueva fecha de inicio, o null si se canceló. */
    public LocalDate pedirNuevaFechaInicio(Planificacion p, LocalDate sugerida) {
        JSpinner fecha = Ui.selectorFecha(sugerida);
        JPanel panel = new JPanel(new GridBagLayout());
        Ui.fila(panel, 0, "Nueva fecha de inicio", fecha);
        Ui.fila(panel, 1, "", Ui.secundario("<html>Todos los eventos activos se mueven la misma cantidad<br>"
                + "de días y pasan a REPROGRAMADO.</html>"));
        int opcion = JOptionPane.showConfirmDialog(this, panel, "Reprogramar \"" + p.getNombre() + "\"",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return opcion == JOptionPane.OK_OPTION ? Ui.getFecha(fecha) : null;
    }

    private void mostrarEventos() {
        modeloEventos.setRowCount(0);
        Planificacion p = getSeleccionada();
        if (p == null) {
            tituloEventos.setText("Eventos de la planificación");
            return;
        }
        tituloEventos.setText("Eventos de \"" + p.getNombre() + "\"");
        for (Evento e : p.getEventos()) {
            modeloEventos.addRow(new Object[] {e.getIdEvento(), Formato.fecha(e.getFecha()), e.getHorarioCorto(),
                    e.getTipo(), e.getLocacion().getNombre(), e.getCantidadAsistentes(), e.getEstado().getNombre()});
        }
    }

    private void actualizarBotones() {
        boolean hay = getSeleccionada() != null;
        botonAgregar.setEnabled(hay);
        botonRenombrar.setEnabled(hay);
        botonReprogramar.setEnabled(hay && puedeValidar);
        botonCancelar.setEnabled(hay && puedeValidar);
    }

    private static void configurar(JTable tabla) {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
    }

    private static DefaultTableModel tablaModelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }
}
