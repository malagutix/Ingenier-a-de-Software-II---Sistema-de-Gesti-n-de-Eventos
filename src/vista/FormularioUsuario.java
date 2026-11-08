package vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Usuario;

/**
 * Formulario reutilizable para registrar (CDU 001) y modificar (CDU 006) usuarios.
 */
class FormularioUsuario extends JPanel {

    private static final long serialVersionUID = 1L;

    private final boolean esModificacion;
    private final JTextField nombre = new JTextField(22);
    private final JTextField apellido = new JTextField(22);
    private final JTextField dni = new JTextField(22);
    private final JTextField correo = new JTextField(22);
    private final JPasswordField contrasena = new JPasswordField(22);
    private final JPasswordField confirmacion = new JPasswordField(22);

    FormularioUsuario(boolean esModificacion) {
        super(new GridBagLayout());
        this.esModificacion = esModificacion;

        Ui.fila(this, 0, "Nombre", nombre);
        Ui.fila(this, 1, "Apellido", apellido);
        Ui.fila(this, 2, "DNI", dni);
        Ui.fila(this, 3, "Correo electrónico", correo);
        Ui.fila(this, 4, esModificacion ? "Nueva contraseña" : "Contraseña", contrasena);
        Ui.fila(this, 5, "Confirmar contraseña", confirmacion);

        JLabel ayuda = Ui.secundario(esModificacion
                ? "Dejá la contraseña vacía para no cambiarla."
                : "Mínimo 8 caracteres, con letras y números. Todos los campos son obligatorios.");
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 6;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(8, 0, 0, 0);
        add(ayuda, c);
    }

    void cargar(Usuario usuario) {
        nombre.setText(usuario.getNombre());
        apellido.setText(usuario.getApellido());
        dni.setText(usuario.getDni());
        correo.setText(usuario.getCorreoElectronico());
        contrasena.setText("");
        confirmacion.setText("");
    }

    /** Devuelve los datos cargados, o null si las contraseñas no coinciden (ya se avisó). */
    DatosUsuario leer() {
        String clave = new String(contrasena.getPassword());
        String confirmar = new String(confirmacion.getPassword());
        if (!clave.equals(confirmar)) {
            Ui.error(this, "Contraseñas no coinciden");
            return null;
        }
        if (esModificacion && clave.isEmpty()) {
            clave = null; // no se cambia
        }
        return new DatosUsuario(nombre.getText(), apellido.getText(), dni.getText(),
                correo.getText(), clave);
    }
}
