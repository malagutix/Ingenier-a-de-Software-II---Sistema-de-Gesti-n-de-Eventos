package vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

import modelo.eventos.entidades.DatosEvento;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.TipoEvento;

/** Formulario para modificar un evento (CDU 014). La fecha se cambia reprogramando. */
class FormularioEvento extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JComboBox<TipoEvento> tipo = new JComboBox<>();
    private final JSpinner hora;
    private final JSpinner duracion = Ui.selectorNumero(4, 1, 24);
    private final JSpinner asistentes = Ui.selectorNumero(50, 1, 100_000);
    private final JComboBox<Locacion> locacion = new JComboBox<>();
    private final JTextArea requerimientos = new JTextArea(4, 28);

    FormularioEvento(Evento evento, List<TipoEvento> tiposActivos, List<Locacion> locacionesHabilitadas) {
        super(new GridBagLayout());
        for (TipoEvento t : tiposActivos) {
            tipo.addItem(t);
        }
        for (Locacion l : locacionesHabilitadas) {
            locacion.addItem(l);
        }
        hora = Ui.selectorHora(evento.getHoraInicio());
        requerimientos.setLineWrap(true);
        requerimientos.setWrapStyleWord(true);

        Ui.fila(this, 0, "Fecha", new JLabel(Formato.fecha(evento.getFecha())
                + "   (para cambiarla, usá Validar > Reprogramar)"));
        Ui.fila(this, 1, "Tipo de evento", tipo);
        Ui.fila(this, 2, "Hora de inicio", hora);
        Ui.fila(this, 3, "Duración (horas)", duracion);
        Ui.fila(this, 4, "Cantidad de asistentes", asistentes);
        Ui.fila(this, 5, "Ubicación", locacion);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 6;
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(8, 0, 4, 10);
        add(new JLabel("Requerimientos específicos"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 0, 4, 0);
        add(new JScrollPane(requerimientos), c);

        seleccionar(tipo, evento.getTipo());
        duracion.setValue(evento.getDuracionHoras());
        asistentes.setValue(evento.getCantidadAsistentes());
        seleccionar(locacion, evento.getLocacion());
        requerimientos.setText(evento.getRequerimientosEspecificos());
    }

    /** Selecciona el elemento; si no está (desactivado o inhabilitado), lo agrega. */
    private static <T> void seleccionar(JComboBox<T> combo, T elemento) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i) == elemento) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.addItem(elemento);
        combo.setSelectedItem(elemento);
    }

    DatosEvento leer() {
        TipoEvento t = (TipoEvento) tipo.getSelectedItem();
        Locacion l = (Locacion) locacion.getSelectedItem();
        return new DatosEvento(t == null ? null : t.getIdTipoEvento(), Ui.getHora(hora), Ui.getNumero(duracion),
                Ui.getNumero(asistentes), l == null ? null : l.getIdLocacion(), requerimientos.getText());
    }
}
