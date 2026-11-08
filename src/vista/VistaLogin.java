package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import modelo.usuarios.entidades.CorreoSimulado;
import modelo.usuarios.entidades.DatosUsuario;

/**
 * Vista "Login" del DCO 2: iniciar sesión (CDU 002), crear cuenta (CDU 001)
 * y restablecer contraseña (CDU 003). Las acciones las resuelve ControladorLogin.
 */
public class VistaLogin extends JPanel implements Vista {

    private static final long serialVersionUID = 1L;

    /** Lo que eligió el usuario en el diálogo "Restablecer contraseña". */
    public enum AccionRecuperacion { ENVIAR_CODIGO, YA_TENGO_CODIGO, CANCELAR }

    /** Respuesta del diálogo "Restablecer contraseña". */
    public static final class PedidoRecuperacion {
        private final AccionRecuperacion accion;
        private final String correo;

        PedidoRecuperacion(AccionRecuperacion accion, String correo) {
            this.accion = accion;
            this.correo = correo;
        }

        public AccionRecuperacion accion() { return accion; }
        public String correo() { return correo; }
    }

    /** Lo que hace el controlador con el código y la contraseña nueva. Devuelve true si se pudo. */
    public interface AccionNuevaContrasena {
        boolean aplicar(String codigo, String nueva, String confirmacion);
    }

    private final JTextField correo = new JTextField(24);
    private final JPasswordField contrasena = new JPasswordField(24);
    private final JButton botonIngresar = new JButton("Ingresar");
    private Runnable alIngresar = () -> { };
    private Runnable alCrearCuenta = () -> { };
    private Runnable alOlvidarContrasena = () -> { };
    private Runnable alAbrirBandeja = () -> { };

    /**
     * @param mostrarBandeja   true si el correo es simulado (muestra el enlace a la bandeja)
     * @param usuariosDePrueba texto con los usuarios de ejemplo, o null para no mostrarlo
     * @param aviso            dónde se guardan los datos (base de datos o memoria)
     */
    public VistaLogin(boolean mostrarBandeja, String usuariosDePrueba, String aviso) {
        super(new GridBagLayout());
        setBackground(Ui.FONDO_SUAVE);

        JPanel columna = new JPanel();
        columna.setOpaque(false);
        columna.setLayout(new BoxLayout(columna, BoxLayout.Y_AXIS));
        columna.add(crearTarjetaLogin(mostrarBandeja));
        if (usuariosDePrueba != null) {
            columna.add(Box.createVerticalStrut(14));
            columna.add(crearTarjetaPrueba(usuariosDePrueba));
        }
        if (aviso != null) {
            columna.add(Box.createVerticalStrut(10));
            JLabel etiqueta = Ui.secundario(aviso);
            etiqueta.setAlignmentX(LEFT_ALIGNMENT);
            columna.add(etiqueta);
        }
        add(columna);

        botonIngresar.addActionListener(e -> alIngresar.run());
        contrasena.addActionListener(e -> alIngresar.run());
        correo.addActionListener(e -> contrasena.requestFocusInWindow());
    }

    // ---------------------------------------------------------------
    // Eventos que escucha el controlador
    // ---------------------------------------------------------------

    public void alIngresar(Runnable accion) { this.alIngresar = accion; }
    public void alCrearCuenta(Runnable accion) { this.alCrearCuenta = accion; }
    public void alOlvidarContrasena(Runnable accion) { this.alOlvidarContrasena = accion; }
    public void alAbrirBandeja(Runnable accion) { this.alAbrirBandeja = accion; }

    // ---------------------------------------------------------------
    // Datos de la pantalla
    // ---------------------------------------------------------------

    public String getCorreo() { return correo.getText(); }
    public String getContrasena() { return new String(contrasena.getPassword()); }
    public void setCorreo(String texto) { correo.setText(texto); }
    public void limpiarContrasena() { contrasena.setText(""); }

    /** Después de un ingreso fallido: deja la contraseña seleccionada para reescribirla. */
    public void seleccionarContrasena() {
        contrasena.selectAll();
        contrasena.requestFocusInWindow();
    }

    /** Al volver al login (cerrar sesión): vacía los campos. */
    public void limpiar() {
        correo.setText(""); // si quedara el del usuario anterior, se propondría al recuperar la contraseña
        contrasena.setText("");
        SwingUtilities.invokeLater(correo::requestFocusInWindow);
    }

    public JButton getBotonIngresar() {
        return botonIngresar;
    }

    @Override
    public Component componente() {
        return this;
    }

    // ---------------------------------------------------------------
    // Diálogos
    // ---------------------------------------------------------------

    /** CDU 001: formulario de registro. Se repite hasta que el controlador lo acepte o se cancele. */
    public boolean pedirNuevoUsuario(Predicate<DatosUsuario> alAceptar) {
        FormularioUsuario formulario = new FormularioUsuario(false);
        return Ui.dialogoHastaExito(this, formulario, "Crear cuenta", formulario::leer, alAceptar);
    }

    /** CDU 003, paso 1: pide el correo de la cuenta. */
    public PedidoRecuperacion pedirCorreoRecuperacion() {
        JTextField mail = new JTextField(correo.getText(), 26);
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.add(new JLabel("Ingresá el correo electrónico con el que te registraste:"), BorderLayout.NORTH);
        panel.add(mail, BorderLayout.CENTER);
        int opcion = JOptionPane.showOptionDialog(this, panel, "Restablecer contraseña",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new String[] {"Enviar código", "Ya tengo un código", "Cancelar"}, "Enviar código");
        AccionRecuperacion accion = opcion == 0 ? AccionRecuperacion.ENVIAR_CODIGO
                : opcion == 1 ? AccionRecuperacion.YA_TENGO_CODIGO : AccionRecuperacion.CANCELAR;
        return new PedidoRecuperacion(accion, mail.getText().trim());
    }

    /** CorreoSimulado real: avisa que se envió el código. */
    public void avisarCodigoEnviado(String direccion) {
        Ui.info(this, "Te enviamos un código de recuperación a " + direccion + ".\n"
                + "Revisá tu correo (también la carpeta de spam) e ingresalo a continuación.");
    }

    /** CorreoSimulado simulado: avisa que se envió y pregunta si quiere verlo ahora. */
    public boolean preguntarVerCorreo(String direccion) {
        int ver = JOptionPane.showOptionDialog(this,
                "Se envió un código de recuperación a " + direccion + ".\n\n"
                        + "Como el correo es simulado, podés verlo ahora o más tarde\n"
                        + "desde \"Bandeja de correo (simulada)\".",
                "Correo enviado", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new String[] {"Ver el correo", "Cerrar"}, "Ver el correo");
        return ver == 0;
    }

    /** Muestra la bandeja simulada y devuelve el correo elegido, o null. */
    public CorreoSimulado elegirCorreo(List<CorreoSimulado> correos) {
        JList<CorreoSimulado> lista = new JList<>(correos.toArray(new CorreoSimulado[0]));
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setSelectedIndex(0);
        lista.setVisibleRowCount(Math.min(8, correos.size()));
        int opcion = JOptionPane.showOptionDialog(this, new JScrollPane(lista),
                "Bandeja de correo (simulada)", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[] {"Abrir", "Cerrar"}, "Abrir");
        return opcion == 0 ? lista.getSelectedValue() : null;
    }

    /** Muestra un correo simulado. Devuelve true si el usuario quiere ingresar el código. */
    public boolean mostrarCorreo(CorreoSimulado mail) {
        JTextArea cuerpo = new JTextArea(
                "Para: " + mail.destinatario() + "\n"
                        + "Asunto: Recuperación de contraseña\n\n"
                        + "Hola. Recibimos un pedido para restablecer tu contraseña.\n\n"
                        + "Tu código de recuperación es:  " + mail.codigo() + "\n\n"
                        + "Ingresalo en la aplicación junto con tu nueva contraseña.\n"
                        + "Vence en 30 minutos y se puede usar una sola vez.");
        cuerpo.setEditable(false);
        cuerpo.setColumns(46);
        cuerpo.setRows(8);
        int opcion = JOptionPane.showOptionDialog(this, new JScrollPane(cuerpo), "Correo recibido",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[] {"Ingresar el código", "Cerrar"}, "Ingresar el código");
        return opcion == 0;
    }

    /**
     * CDU 003, paso 2: código y contraseña nueva. Se repite (con los datos
     * cargados) hasta que el controlador lo acepte o se cancele.
     *
     * @param cuenta correo de la cuenta a la que se envió el código, o null si no se sabe
     */
    public void pedirNuevaContrasena(String codigoInicial, String cuenta, AccionNuevaContrasena alAceptar) {
        JTextField codigo = new JTextField(codigoInicial, 12);
        JPasswordField nueva = new JPasswordField(20);
        JPasswordField confirmacion = new JPasswordField(20);
        JPanel formulario = new JPanel(new GridBagLayout());
        int fila = 0;
        if (cuenta != null) {
            Ui.fila(formulario, fila++, "Cuenta", new JLabel("<html><b>" + cuenta + "</b></html>"));
        }
        Ui.fila(formulario, fila++, "Código recibido", codigo);
        Ui.fila(formulario, fila++, "Nueva contraseña", nueva);
        Ui.fila(formulario, fila, "Confirmar contraseña", confirmacion);
        Ui.dialogoHastaExito(this, formulario, "Nueva contraseña", () -> Boolean.TRUE,
                ok -> alAceptar.aplicar(codigo.getText(), new String(nueva.getPassword()),
                        new String(confirmacion.getPassword())));
    }

    // ---------------------------------------------------------------
    // Armado de la pantalla
    // ---------------------------------------------------------------

    private JPanel crearTarjetaLogin(boolean mostrarBandeja) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 12));
        tarjeta.setBackground(java.awt.Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(0xD3, 0xD1, 0xC7)), Ui.margen(24)));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.add(Ui.titulo("Iniciar sesión"));
        encabezado.add(Box.createVerticalStrut(4));
        encabezado.add(Ui.secundario("Sistema de Gestión de Eventos"));
        tarjeta.add(encabezado, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        Ui.fila(formulario, 0, "Correo electrónico", correo);
        Ui.fila(formulario, 1, "Contraseña", contrasena);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 2;
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_END;
        c.insets = new Insets(10, 0, 0, 0);
        formulario.add(botonIngresar, c);
        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel enlaces = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        enlaces.setOpaque(false);
        enlaces.add(enlace("Crear cuenta", () -> alCrearCuenta.run()));
        enlaces.add(new JLabel("   |   "));
        enlaces.add(enlace("Olvidé mi contraseña", () -> alOlvidarContrasena.run()));
        if (mostrarBandeja) {
            enlaces.add(new JLabel("   |   "));
            enlaces.add(enlace("Bandeja de correo (simulada)", () -> alAbrirBandeja.run()));
        }
        tarjeta.add(enlaces, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaPrueba(String usuariosDePrueba) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setOpaque(false);
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.setBorder(Ui.tarjeta());
        tarjeta.add(Ui.subtitulo("Usuarios de prueba"), BorderLayout.NORTH);
        JTextArea texto = new JTextArea(usuariosDePrueba);
        texto.setEditable(false);
        texto.setOpaque(false);
        texto.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        texto.setForeground(Ui.TEXTO_SECUNDARIO);
        tarjeta.add(texto, BorderLayout.CENTER);
        return tarjeta;
    }

    private JLabel enlace(String texto, Runnable accion) {
        JLabel label = new JLabel("<html><u>" + texto + "</u></html>");
        label.setForeground(Ui.ACENTO);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                accion.run();
            }
        });
        return label;
    }
}
