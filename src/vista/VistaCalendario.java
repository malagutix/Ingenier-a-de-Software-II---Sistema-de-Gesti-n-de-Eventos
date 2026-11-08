package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;

/**
 * Vista del calendario mensual de eventos (VisualizarCalendario). Muestra lo
 * que le pasa ControladorCalendario: los eventos del mes y, si no hay, el próximo.
 */
public class VistaCalendario extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;
    private static final Locale ES = Locale.forLanguageTag("es-AR");

    private final JLabel tituloMes = Ui.titulo("");
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel resumen = new JLabel(" ");
    private final JButton anterior = new JButton("< Anterior");
    private final JButton hoy = new JButton("Hoy");
    private final JButton siguiente = new JButton("Siguiente >");

    /** @param veTodos true si el usuario ve todos los eventos; false si solo los suyos */
    public VistaCalendario(boolean veTodos) {
        super(new BorderLayout(0, 10));
        setBorder(Ui.margen(16));

        JPanel arriba = new JPanel(new BorderLayout());
        JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        navegacion.add(anterior);
        navegacion.add(hoy);
        navegacion.add(siguiente);
        arriba.add(tituloMes, BorderLayout.WEST);
        arriba.add(navegacion, BorderLayout.EAST);
        arriba.add(Ui.secundario(veTodos ? "Todos los eventos del sistema." : "Tus eventos."), BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        tabla.setRowHeight(78);
        tabla.setCellSelectionEnabled(false);
        tabla.setRowSelectionAllowed(false);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setShowGrid(true);
        tabla.setGridColor(new Color(0xD3, 0xD1, 0xC7));
        tabla.setDefaultRenderer(Object.class, new RenderDia());
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(resumen, BorderLayout.SOUTH);
    }

    public void alAnterior(Runnable accion) { anterior.addActionListener(e -> accion.run()); }
    public void alHoy(Runnable accion) { hoy.addActionListener(e -> accion.run()); }
    public void alSiguiente(Runnable accion) { siguiente.addActionListener(e -> accion.run()); }

    /**
     * Dibuja el mes.
     *
     * @param proximo próximo evento a partir de hoy (se muestra si el mes no tiene eventos), o null
     */
    public void mostrarMes(YearMonth mes, List<Evento> eventos, Evento proximo) {
        String nombreMes = mes.getMonth().getDisplayName(TextStyle.FULL, ES);
        tituloMes.setText(Character.toUpperCase(nombreMes.charAt(0)) + nombreMes.substring(1) + " " + mes.getYear());

        Map<Integer, List<Evento>> porDia = eventos.stream()
                .collect(Collectors.groupingBy(e -> e.getFecha().getDayOfMonth()));

        modeloTabla.setRowCount(0);
        int desfase = mes.atDay(1).getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        int totalCeldas = desfase + mes.lengthOfMonth();
        int semanas = (totalCeldas + 6) / 7;
        for (int semana = 0; semana < semanas; semana++) {
            Object[] fila = new Object[7];
            for (int d = 0; d < 7; d++) {
                int dia = semana * 7 + d - desfase + 1;
                if (dia >= 1 && dia <= mes.lengthOfMonth()) {
                    fila[d] = new Dia(mes.atDay(dia), porDia.getOrDefault(dia, java.util.Collections.<Evento>emptyList()));
                }
            }
            modeloTabla.addRow(fila);
        }
        resumen.setForeground(Ui.TEXTO_SECUNDARIO);
        if (!eventos.isEmpty()) {
            resumen.setText(eventos.size() + " evento(s) este mes. Pasá el mouse sobre un día para ver el detalle.");
        } else if (proximo != null) {
            resumen.setText("No hay eventos este mes. Próximo evento: #" + proximo.getIdEvento() + " "
                    + proximo.getTipo() + " el " + Formato.fecha(proximo.getFecha()) + ".");
        } else {
            resumen.setText("No hay eventos este mes.");
        }
    }

    @Override
    public Component componente() {
        return this;
    }

    /** Contenido de una celda del calendario. */
    private static final class Dia {
        private final LocalDate fecha;
        private final List<Evento> eventos;

        Dia(LocalDate fecha, List<Evento> eventos) {
            this.fecha = fecha;
            this.eventos = eventos;
        }

        LocalDate fecha() { return fecha; }
        List<Evento> eventos() { return eventos; }
    }

    /** Dibuja el número de día y los eventos de ese día. */
    private static class RenderDia extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                                                       boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, "", false, false, fila, columna);
            setVerticalAlignment(SwingConstants.TOP);
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            setBackground(Color.WHITE);
            if (!(valor instanceof Dia)) {
                setBackground(new Color(0xF4, 0xF3, 0xEF));
                return this;
            }
            Dia dia = (Dia) valor;
            boolean esHoy = dia.fecha().equals(LocalDate.now());
            StringBuilder html = new StringBuilder("<html>");
            html.append(esHoy ? "<b style='color:#534AB7'>" : "<b>").append(dia.fecha().getDayOfMonth()).append("</b>");
            for (Evento e : dia.eventos()) {
                html.append("<br><span style='font-size:9px'>").append(Formato.hora(e.getHoraInicio())).append(" #")
                        .append(e.getIdEvento()).append(' ').append(e.getTipo()).append(" · ")
                        .append(e.getEstado().getNombre()).append("</span>");
            }
            html.append("</html>");
            setText(html.toString());
            if (!dia.eventos().isEmpty()) {
                setBackground(new Color(0xEE, 0xED, 0xFE));
            }
            StringBuilder tooltip = new StringBuilder("<html>");
            for (Evento e : dia.eventos()) {
                tooltip.append("#").append(e.getIdEvento()).append(" ").append(e.getHorario()).append(" ")
                        .append(e.getTipo()).append(" (")
                        .append(e.getPlanificacion().getNombre()).append(", ")
                        .append(e.getPlanificacion().posicionDe(e)).append(") - ")
                        .append(e.getLocacion().getNombre()).append(" - ").append(e.getCantidadAsistentes())
                        .append(" asistentes - ").append(e.getEstado().getNombre()).append("<br>");
            }
            setToolTipText(dia.eventos().isEmpty() ? null : tooltip.append("</html>").toString());
            return this;
        }
    }
}
