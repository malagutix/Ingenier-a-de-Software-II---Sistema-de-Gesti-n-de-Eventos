package vista;

import java.time.LocalDate;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import modelo.eventos.entidades.DatosSolicitud;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.MenuEvento;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.entidades.TipoEvento;
import modelo.usuarios.entidades.Usuario;

/** Formulario de solicitud (CDU 008 y CDU 009). */
class FormularioSolicitud extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Envoltorio para mostrar el cliente con su nombre en el combo. */
    private static final class OpcionCliente {
        private final Usuario usuario;

        OpcionCliente(Usuario usuario) {
            this.usuario = usuario;
        }

        Usuario usuario() {
            return usuario;
        }

        @Override
        public String toString() {
            return usuario.getNombre() + " " + usuario.getApellido() + " (" + usuario.getCorreoElectronico() + ")";
        }
    }

    private final JComboBox<OpcionCliente> cliente = new JComboBox<>();
    private final JSpinner fecha = Ui.selectorFecha(LocalDate.now().plusDays(7));
    private final JComboBox<TipoEvento> tipo = new JComboBox<>();
    private final JSpinner invitados = Ui.selectorNumero(50, 1, 100_000);
    private final JComboBox<Locacion> locacion = new JComboBox<>();
    private final JComboBox<MenuEvento> menu = new JComboBox<>(MenuEvento.values());

    FormularioSolicitud(List<Usuario> clientes, List<TipoEvento> tipos, List<Locacion> locaciones) {
        super(new java.awt.GridBagLayout());
        for (Usuario c : clientes) {
            cliente.addItem(new OpcionCliente(c));
        }
        for (TipoEvento t : tipos) {
            tipo.addItem(t);
        }
        for (Locacion l : locaciones) {
            locacion.addItem(l);
        }

        Ui.fila(this, 0, "Cliente", cliente);
        Ui.fila(this, 1, "Fecha solicitada", fecha);
        Ui.fila(this, 2, "Tipo de evento", tipo);
        Ui.fila(this, 3, "Número de invitados", invitados);
        Ui.fila(this, 4, "Lugar preferido", locacion);
        Ui.fila(this, 5, "Menú", menu);
    }

    void cargar(SolicitudEvento s) {
        for (int i = 0; i < cliente.getItemCount(); i++) {
            if (cliente.getItemAt(i).usuario().getIdUsuario() == s.getCliente().getIdUsuario()) {
                cliente.setSelectedIndex(i);
            }
        }
        Ui.setFecha(fecha, s.getFechaSolicitada());
        seleccionar(tipo, s.getTipoEvento());
        invitados.setValue(s.getCantidadInvitados());
        seleccionar(locacion, s.getLocacion());
        menu.setSelectedItem(s.getMenu());
    }

    /** Selecciona el elemento; si no está (por ejemplo, desactivado), lo agrega. */
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

    DatosSolicitud leer() {
        OpcionCliente c = (OpcionCliente) cliente.getSelectedItem();
        TipoEvento t = (TipoEvento) tipo.getSelectedItem();
        Locacion l = (Locacion) locacion.getSelectedItem();
        return new DatosSolicitud(
                c == null ? null : c.usuario().getIdUsuario(),
                Ui.getFecha(fecha),
                t == null ? null : t.getIdTipoEvento(),
                Ui.getNumero(invitados),
                l == null ? null : l.getIdLocacion(),
                (MenuEvento) menu.getSelectedItem());
    }
}
