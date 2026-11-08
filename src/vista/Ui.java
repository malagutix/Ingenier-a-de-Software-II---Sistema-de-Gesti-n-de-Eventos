package vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Utilidades comunes de la vista: estilos, selectores y diálogos. */
final class Ui {

    static final Color ACENTO = new Color(0x53, 0x4A, 0xB7);
    static final Color TEXTO_SECUNDARIO = new Color(0x5F, 0x5E, 0x5A);
    static final Color FONDO_SUAVE = new Color(0xF4, 0xF3, 0xEF);
    static final Color EXITO = new Color(0x3B, 0x6D, 0x11);
    static final Color ERROR = new Color(0xA3, 0x2D, 0x2D);

    private Ui() {
    }

    static void configurarAspecto() {
        try {
            String sistema = UIManager.getSystemLookAndFeelClassName();
            if (sistema.contains("Metal")) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        sistema = info.getClassName();
                    }
                }
            }
            UIManager.setLookAndFeel(sistema);
        } catch (Exception e) {
            // Si falla, queda el aspecto por defecto de Java
        }
    }

    static JLabel titulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 20f));
        return label;
    }

    static JLabel subtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
        return label;
    }

    static JLabel secundario(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(TEXTO_SECUNDARIO);
        return label;
    }

    static Border margen(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    static Border tarjeta() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD3, 0xD1, 0xC7)),
                margen(16));
    }

    /** Agrega una fila "etiqueta: campo" a un panel con GridBagLayout. */
    static void fila(JPanel panel, int fila, String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 0, 4, 10);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        panel.add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        panel.add(campo, c);
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un formulario en un diálogo y, al aceptar, le pasa los datos al
     * controlador. Si el controlador informa que no se pudo (porque el modelo
     * los rechazó), vuelve a abrir el formulario con los datos cargados, hasta
     * que salga bien o el usuario cancele.
     *
     * @param leer      lee los datos del formulario; devuelve null si la propia vista ya
     *                  mostró un error de carga (por ejemplo, contraseñas distintas)
     * @param alAceptar acción del controlador; devuelve true si se pudo realizar
     * @return true si la acción se realizó
     */
    static <T> boolean dialogoHastaExito(Component padre, JComponent formulario, String titulo,
                                         java.util.function.Supplier<T> leer,
                                         java.util.function.Predicate<T> alAceptar) {
        while (true) {
            int opcion = JOptionPane.showConfirmDialog(padre, formulario, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (opcion != JOptionPane.OK_OPTION) {
                return false;
            }
            T datos = leer.get();
            if (datos != null && alAceptar.test(datos)) {
                return true;
            }
        }
    }

    /** Define el ancho preferido de cada columna de una tabla. */
    static void anchos(javax.swing.JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    /** Selector de fecha con formato dd/MM/aaaa. */
    static javax.swing.JSpinner selectorFecha(java.time.LocalDate inicial) {
        javax.swing.SpinnerDateModel modeloFecha = new javax.swing.SpinnerDateModel();
        javax.swing.JSpinner spinner = new javax.swing.JSpinner(modeloFecha);
        spinner.setEditor(new javax.swing.JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        setFecha(spinner, inicial);
        return spinner;
    }

    static void setFecha(javax.swing.JSpinner spinner, java.time.LocalDate fecha) {
        spinner.setValue(java.util.Date.from(fecha.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
    }

    static java.time.LocalDate getFecha(javax.swing.JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException e) {
            // queda el último valor válido
        }
        return ((java.util.Date) spinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    /** Selector de hora con formato HH:mm. */
    static javax.swing.JSpinner selectorHora(java.time.LocalTime inicial) {
        javax.swing.SpinnerDateModel modeloFecha = new javax.swing.SpinnerDateModel();
        modeloFecha.setCalendarField(java.util.Calendar.MINUTE);
        javax.swing.JSpinner spinner = new javax.swing.JSpinner(modeloFecha);
        spinner.setEditor(new javax.swing.JSpinner.DateEditor(spinner, "HH:mm"));
        setHora(spinner, inicial);
        return spinner;
    }

    static void setHora(javax.swing.JSpinner spinner, java.time.LocalTime hora) {
        java.time.LocalTime h = hora == null ? java.time.LocalTime.of(9, 0) : hora;
        spinner.setValue(java.util.Date.from(java.time.LocalDate.of(2000, 1, 1).atTime(h)
                .atZone(java.time.ZoneId.systemDefault()).toInstant()));
    }

    static java.time.LocalTime getHora(javax.swing.JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException e) {
            // queda el último valor válido
        }
        java.time.LocalTime h = ((java.util.Date) spinner.getValue()).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalTime();
        return h.withSecond(0).withNano(0);
    }

    /** Selector decimal (por ejemplo, kilómetros). */
    static javax.swing.JSpinner selectorDecimal(double inicial, double minimo, double maximo, double paso) {
        javax.swing.JSpinner spinner = new javax.swing.JSpinner(
                new javax.swing.SpinnerNumberModel(inicial, minimo, maximo, paso));
        spinner.setEditor(new javax.swing.JSpinner.NumberEditor(spinner, "0.0"));
        return spinner;
    }

    static double getDecimal(javax.swing.JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException e) {
            // queda el último valor válido
        }
        return ((Number) spinner.getValue()).doubleValue();
    }

    /** Selector numérico entero. */
    static javax.swing.JSpinner selectorNumero(int inicial, int minimo, int maximo) {
        return new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(inicial, minimo, maximo, 1));
    }

    static int getNumero(javax.swing.JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException e) {
            // queda el último valor válido
        }
        return (Integer) spinner.getValue();
    }
}
