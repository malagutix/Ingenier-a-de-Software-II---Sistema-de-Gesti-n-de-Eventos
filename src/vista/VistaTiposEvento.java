package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.DatosTipoEvento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.TipoEvento;

/**
 * Vista de gestión de tipos de evento: permite incorporar tipos nuevos sin
 * modificar el sistema. Las acciones las resuelve ControladorTiposEvento.
 */
public class VistaTiposEvento extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(new String[] {
            "ID", "Nombre", "Hora sugerida", "Duración sugerida", "Requerimientos sugeridos", "Estado"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final transient List<TipoEvento> filas = new ArrayList<>();
    private final JButton botonNuevo = new JButton("Nuevo tipo");
    private final JButton botonModificar = new JButton("Modificar");
    private final JButton botonActivar = new JButton("Desactivar");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JLabel estado = new JLabel(" ");
    private Runnable alModificar = () -> { };

    public VistaTiposEvento() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Tipos de evento"), BorderLayout.WEST);
        arriba.add(botonNuevo, BorderLayout.EAST);
        arriba.add(Ui.secundario("Los tipos desactivados no se ofrecen en solicitudes nuevas, "
                + "pero se mantienen en lo ya cargado."), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.setFillsViewportHeight(true);
        Ui.anchos(tabla, 30, 170, 95, 110, 380, 80);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(botonModificar);
        botones.add(botonActivar);
        botones.add(botonEliminar);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(botones, BorderLayout.EAST);
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

    public void alNuevo(Runnable accion) { botonNuevo.addActionListener(e -> accion.run()); }
    public void alModificar(Runnable accion) { this.alModificar = accion; }
    public void alCambiarActivacion(Runnable accion) { botonActivar.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }

    public void mostrarTipos(List<TipoEvento> tipos) {
        filas.clear();
        modeloTabla.setRowCount(0);
        filas.addAll(tipos);
        for (TipoEvento t : filas) {
            modeloTabla.addRow(new Object[] {t.getIdTipoEvento(), t.getNombre(), Formato.hora(t.getHoraInicioSugerida()),
                    t.getDuracionSugerida() + " h", t.getRequerimientosSugeridos(),
                    t.isActivo() ? "Activo" : "Inactivo"});
        }
        actualizarBotones();
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar la cantidad */
    public void mostrarEstado(String mensajeExito) {
        estado.setForeground(mensajeExito != null ? Ui.EXITO : Ui.TEXTO_SECUNDARIO);
        estado.setText(mensajeExito != null ? mensajeExito : filas.size() + " tipo(s)");
    }

    public TipoEvento getSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 || fila >= filas.size() ? null : filas.get(fila);
    }

    /**
     * Formulario de tipo de evento. Se repite hasta que el controlador lo acepte o se cancele.
     *
     * @param actual el tipo a modificar, o null si es nuevo
     */
    public boolean editarTipo(String titulo, TipoEvento actual, Predicate<DatosTipoEvento> alAceptar) {
        FormularioTipoEvento formulario = new FormularioTipoEvento();
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
        TipoEvento t = getSeleccionado();
        boolean hay = t != null;
        botonModificar.setEnabled(hay);
        botonActivar.setEnabled(hay);
        botonEliminar.setEnabled(hay);
        botonActivar.setText(t != null && !t.isActivo() ? "Activar" : "Desactivar");
    }
}
