package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import modelo.eventos.entidades.Formato;
import modelo.usuarios.entidades.Notificacion;

/**
 * Vista de las notificaciones que el usuario recibió como Observer de los
 * eventos (cambios de estado, modificaciones, eliminaciones). Se pueden
 * marcar como leídas y eliminar, de a una, varias o todas.
 */
public class VistaNotificaciones extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    private final DefaultListModel<Notificacion> modeloLista = new DefaultListModel<>();
    private final JList<Notificacion> lista = new JList<>(modeloLista);
    private final JButton botonLeida = new JButton("Marcar como leída");
    private final JButton botonTodasLeidas = new JButton("Marcar todas como leídas");
    private final JButton botonEliminar = new JButton("Eliminar");
    private final JButton botonEliminarTodas = new JButton("Eliminar todas");
    private final JLabel estado = new JLabel(" ");

    public VistaNotificaciones() {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));
        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(Ui.titulo("Notificaciones"), BorderLayout.NORTH);
        arriba.add(Ui.secundario("Avisos de los eventos en los que participás. Los más recientes primero;"
                + " las no leídas, en negrita."), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        lista.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lista.setFixedCellHeight(26);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> l, Object valor, int indice,
                                                          boolean seleccionada, boolean foco) {
                Notificacion n = (Notificacion) valor;
                super.getListCellRendererComponent(l, (n.isLeida() ? "    " : "●  ")
                        + Formato.fechaHora(n.getCreada()) + "   " + n.getMensaje(), indice, seleccionada, foco);
                setFont(getFont().deriveFont(n.isLeida() ? Font.PLAIN : Font.BOLD));
                if (n.isLeida() && !seleccionada) {
                    setForeground(Ui.TEXTO_SECUNDARIO);
                }
                return this;
            }
        });
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.add(botonLeida);
        acciones.add(botonTodasLeidas);
        acciones.add(botonEliminar);
        acciones.add(botonEliminarTodas);
        abajo.add(estado, BorderLayout.WEST);
        abajo.add(acciones, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        lista.getSelectionModel().addListSelectionListener(e -> actualizarBotones());
        actualizarBotones();
    }

    public void alMarcarLeidas(Runnable accion) { botonLeida.addActionListener(e -> accion.run()); }
    public void alMarcarTodasLeidas(Runnable accion) { botonTodasLeidas.addActionListener(e -> accion.run()); }
    public void alEliminar(Runnable accion) { botonEliminar.addActionListener(e -> accion.run()); }
    public void alEliminarTodas(Runnable accion) { botonEliminarTodas.addActionListener(e -> accion.run()); }

    /** Muestra las notificaciones (llegan en orden de llegada; se muestran las más recientes primero). */
    public void mostrarNotificaciones(List<Notificacion> notificaciones) {
        modeloLista.clear();
        List<Notificacion> invertidas = new ArrayList<>(notificaciones);
        Collections.reverse(invertidas);
        invertidas.forEach(modeloLista::addElement);
        actualizarBotones();
    }

    /** @param mensajeExito mensaje en verde, o null para mostrar cuántas hay */
    public void mostrarEstado(String mensajeExito) {
        if (mensajeExito != null) {
            estado.setForeground(Ui.EXITO);
            estado.setText(mensajeExito);
            return;
        }
        estado.setForeground(Ui.TEXTO_SECUNDARIO);
        estado.setText(modeloLista.isEmpty() ? "No tenés notificaciones."
                : modeloLista.size() + " notificación(es), " + getNoLeidas().size() + " sin leer");
    }

    public List<Notificacion> getSeleccionadas() {
        return lista.getSelectedValuesList();
    }

    public List<Notificacion> getTodas() {
        return Collections.list(modeloLista.elements());
    }

    public List<Notificacion> getNoLeidas() {
        List<Notificacion> noLeidas = new ArrayList<>();
        for (Notificacion n : getTodas()) {
            if (!n.isLeida()) {
                noLeidas.add(n);
            }
        }
        return noLeidas;
    }

    @Override
    public Component componente() {
        return this;
    }

    private void actualizarBotones() {
        boolean haySeleccion = !lista.isSelectionEmpty();
        botonLeida.setEnabled(getSeleccionadas().stream().anyMatch(n -> !n.isLeida()));
        botonTodasLeidas.setEnabled(!getNoLeidas().isEmpty());
        botonEliminar.setEnabled(haySeleccion);
        botonEliminarTodas.setEnabled(!modeloLista.isEmpty());
    }
}
