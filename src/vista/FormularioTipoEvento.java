package vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import modelo.eventos.entidades.DatosTipoEvento;
import modelo.eventos.entidades.TipoEvento;

/** Formulario de alta y modificación de tipos de evento. */
class FormularioTipoEvento extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTextField nombre = new JTextField(24);
    private final JSpinner hora = Ui.selectorHora(java.time.LocalTime.of(20, 0));
    private final JSpinner duracion = Ui.selectorNumero(5, 1, 24);
    private final JTextArea requerimientos = new JTextArea(3, 24);

    FormularioTipoEvento() {
        super(new GridBagLayout());
        requerimientos.setLineWrap(true);
        requerimientos.setWrapStyleWord(true);
        Ui.fila(this, 0, "Nombre", nombre);
        Ui.fila(this, 1, "Hora de inicio sugerida", hora);
        Ui.fila(this, 2, "Duración sugerida (horas)", duracion);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 3;
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(8, 0, 4, 10);
        add(new JLabel("Requerimientos sugeridos"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 0, 4, 0);
        add(new JScrollPane(requerimientos), c);
        GridBagConstraints a = new GridBagConstraints();
        a.gridy = 4;
        a.gridx = 0;
        a.gridwidth = 2;
        a.anchor = GridBagConstraints.LINE_START;
        a.insets = new Insets(6, 0, 0, 0);
        add(Ui.secundario("<html>Los valores sugeridos se usan al crear eventos de este tipo.<br>"
                + "Cambiarlos no modifica los eventos que ya existen.</html>"), a);
    }

    void cargar(TipoEvento t) {
        nombre.setText(t.getNombre());
        Ui.setHora(hora, t.getHoraInicioSugerida());
        duracion.setValue(t.getDuracionSugerida());
        requerimientos.setText(t.getRequerimientosSugeridos());
    }

    DatosTipoEvento leer() {
        return new DatosTipoEvento(nombre.getText(), Ui.getNumero(duracion), Ui.getHora(hora),
                requerimientos.getText());
    }
}
