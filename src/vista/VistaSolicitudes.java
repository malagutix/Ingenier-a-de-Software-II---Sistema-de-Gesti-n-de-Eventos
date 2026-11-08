package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.DatosSolicitud;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.entidades.TipoEvento;
import modelo.usuarios.entidades.Usuario;

/**
 * Vista de solicitudes del administrador: CDU 008 (crear), CDU 009 (modificar),
 * CDU 010 (eliminar), aceptar y rechazar. Las acciones las resuelve ControladorSolicitudes.
 */
public class VistaSolicitudes extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[] {
            "ID", "Fecha", "Tipo", "Invitados", "Lugar", "Menú", "Cliente", "Estado", "Planificación"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new TablaSeleccionMultiple(modeloTabla); // CDU 010: una o más
    private final transient List<SolicitudEvento> filas = new ArrayList<>();
    private final JButton botonNueva = new JButton("Nueva solicitud");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonAceptar = new JButton("Aceptar");
    private final JButton botonRechazar = new JButton("Rechazar");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JLabel estado = new JLabel(" ");

    public VistaSolicitudes() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Solicitudes"), BorderLayout.WEST);
        arriba.add(Ui.secundario("Hacé clic en una o varias solicitudes para seleccionarlas."), BorderLayout.SOUTH);
        arriba.add(botonNueva, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        Ui.anchos(tabla, 30, 85, 90, 65, 185, 120, 115, 80, 85);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(botonModificar);
        acciones.add(botonAceptar);
        acciones.add(botonRechazar);
        acciones.add(botonEliminar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
    }

    public void alNueva(Runnable accion) { botonNueva.addActionListener(e -> accion.run()); }
    public void alModificar(Runnable accion) { botonModificar.addActionListener(e -> accion.run()); }
    public void alAceptar(Runnable accion) { botonAceptar.addActionListener(e -> accion.run()); }
    public void alRechazar(Runnable accion) { botonRechazar.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }

    /** @param planificaciones texto de la columna Planificación por ID de solicitud ("-" si no tiene) */
    public void mostrarSolicitudes(List<SolicitudEvento> solicitudes, Map<Integer, String> planificaciones) {
        filas.clear();
        modeloTabla.setRowCount(0);
        filas.addAll(solicitudes);
        for (SolicitudEvento s : filas) {
            modeloTabla.addRow(new Object[] {s.getIdSolicitud(), Formato.fecha(s.getFechaSolicitada()),
                    s.getTipoEvento(), s.getCantidadInvitados(), s.getLocacion().getNombre(), s.getMenu(),
                    s.getCliente().getNombre() + " " + s.getCliente().getApellido(), s.getEstado(),
                    planificaciones.getOrDefault(s.getIdSolicitud(), "-")});
        }
        actualizarBotones();
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar la cantidad */
    public void mostrarEstado(String mensajeExito) {
        estado.setForeground(mensajeExito != null ? Ui.EXITO : Ui.TEXTO_SECUNDARIO);
        estado.setText(mensajeExito != null ? mensajeExito : filas.size() + " solicitud(es)");
    }

    /** La solicitud seleccionada, o null si no hay ninguna o hay más de una. */
    public SolicitudEvento getSeleccionada() {
        List<SolicitudEvento> seleccionadas = getSeleccionadas();
        return seleccionadas.size() == 1 ? seleccionadas.get(0) : null;
    }

    /** Todas las solicitudes seleccionadas (CDU 010: una o más), en el orden de la tabla. */
    public List<SolicitudEvento> getSeleccionadas() {
        List<SolicitudEvento> seleccionadas = new ArrayList<>();
        for (int fila : tabla.getSelectedRows()) {
            seleccionadas.add(filas.get(tabla.convertRowIndexToModel(fila)));
        }
        return seleccionadas;
    }

    /**
     * Formulario de solicitud (CDU 008 / 009). Se repite hasta que el
     * controlador lo acepte o se cancele.
     *
     * @param actual la solicitud a modificar, o null si es nueva
     */
    public boolean editarSolicitud(String titulo, List<Usuario> clientes, List<TipoEvento> tipos,
                                   List<Locacion> locaciones, SolicitudEvento actual,
                                   Predicate<DatosSolicitud> alAceptar) {
        FormularioSolicitud formulario = new FormularioSolicitud(clientes, tipos, locaciones);
        if (actual != null) {
            formulario.cargar(actual);
        }
        return Ui.dialogoHastaExito(this, formulario, titulo, formulario::leer, alAceptar);
    }

    @Override
    public Component componente() {
        return this;
    }

    private void actualizarBotones() {
        // Con una sola seleccionada están todas las acciones; con varias, solo Eliminar (en lote)
        SolicitudEvento s = getSeleccionada();
        boolean una = s != null;
        botonModificar.setEnabled(una);
        botonAceptar.setEnabled(una && s.estaPendiente());
        botonRechazar.setEnabled(una && s.estaPendiente());
        botonEliminar.setEnabled(tabla.getSelectedRowCount() > 0);
    }
}
