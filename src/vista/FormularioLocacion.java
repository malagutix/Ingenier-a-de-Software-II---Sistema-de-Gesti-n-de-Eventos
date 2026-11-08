package vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import modelo.eventos.entidades.DatosLocacion;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.TipoLocacion;

/** Formulario de alta y modificación de locaciones. */
class FormularioLocacion extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTextField nombre = new JTextField(28);
    private final JTextField direccion = new JTextField(28);
    private final JComboBox<TipoLocacion> tipo = new JComboBox<>(TipoLocacion.values());
    private final JSpinner capacidad = Ui.selectorNumero(100, 1, 100_000);
    private final JSpinner distancia = Ui.selectorDecimal(5, 0, 10_000, 0.5);
    private final JSpinner traslado = Ui.selectorNumero(20, 0, 24 * 60);
    private final JSpinner montaje = Ui.selectorNumero(60, 0, 12 * 60);
    private final JSpinner vehiculos = Ui.selectorNumero(1, 0, 100);
    private final JCheckBox camaraFrio = new JCheckBox("Requiere cámara de frío");
    private final JTextArea otros = new JTextArea(3, 28);

    FormularioLocacion() {
        super(new GridBagLayout());
        otros.setLineWrap(true);
        otros.setWrapStyleWord(true);
        int f = 0;
        Ui.fila(this, f++, "Nombre", nombre);
        Ui.fila(this, f++, "Dirección", direccion);
        Ui.fila(this, f++, "Tipo de locación", tipo);
        Ui.fila(this, f++, "Capacidad máxima (personas)", capacidad);
        Ui.fila(this, f++, "Distancia a la sede (km)", distancia);
        Ui.fila(this, f++, "Tiempo de traslado (minutos)", traslado);
        Ui.fila(this, f++, "Tiempo de montaje (minutos)", montaje);
        Ui.fila(this, f++, "Vehículos necesarios", vehiculos);
        Ui.fila(this, f++, "", camaraFrio);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = f++;
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(8, 0, 4, 10);
        add(new JLabel("Otros requerimientos logísticos"), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 0, 4, 0);
        add(new JScrollPane(otros), c);
        GridBagConstraints a = new GridBagConstraints();
        a.gridy = f;
        a.gridx = 0;
        a.gridwidth = 2;
        a.anchor = GridBagConstraints.LINE_START;
        a.insets = new Insets(6, 0, 0, 0);
        add(Ui.secundario("<html>El tiempo de montaje se reserva antes y después de cada evento<br>"
                + "(montaje y desmontaje), así dos eventos no se pisan en el mismo lugar.</html>"), a);
    }

    void cargar(Locacion l) {
        nombre.setText(l.getNombre());
        direccion.setText(l.getDireccion());
        tipo.setSelectedItem(l.getTipo());
        capacidad.setValue(l.getCapacidad());
        distancia.setValue(l.getDistanciaKm());
        traslado.setValue(l.getTiempoTrasladoMin());
        montaje.setValue(l.getTiempoMontajeMin());
        vehiculos.setValue(l.getVehiculosRequeridos());
        camaraFrio.setSelected(l.isRequiereCamaraFrio());
        otros.setText(l.getOtrosRequerimientos());
    }

    DatosLocacion leer() {
        return new DatosLocacion(nombre.getText(), direccion.getText(), (TipoLocacion) tipo.getSelectedItem(),
                Ui.getNumero(capacidad), Ui.getDecimal(distancia), Ui.getNumero(traslado), Ui.getNumero(montaje),
                Ui.getNumero(vehiculos), camaraFrio.isSelected(), otros.getText());
    }
}
