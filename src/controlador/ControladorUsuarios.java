package controlador;

import java.util.Collections;
import java.util.List;

import modelo.persistencia.ErrorBaseDatosException;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.excepciones.UsuarioException;
import modelo.usuarios.excepciones.UsuarioNoEncontradoException;
import modelo.usuarios.servicio.GestorUsuarios;
import vista.VistaUsuarios;

/**
 * Controlador de la gestión de usuarios: CDU 004 (buscar y eliminar),
 * CDU 005 (asignar roles) y CDU 006 (modificar).
 */
public class ControladorUsuarios extends Controlador implements Refrescable {

    private final VistaUsuarios vista;
    private final GestorUsuarios gestor;
    private final Usuario admin;
    private final Runnable alCambiarDatosPropios;

    /** @param alCambiarDatosPropios se ejecuta si el administrador modifica sus propios datos */
    public ControladorUsuarios(VistaUsuarios vista, GestorUsuarios gestor, Usuario admin,
                               Runnable alCambiarDatosPropios) {
        this.vista = vista;
        this.gestor = gestor;
        this.admin = admin;
        this.alCambiarDatosPropios = alCambiarDatosPropios;
        vista.alBuscar(() -> refrescar(null));
        vista.alModificar(this::modificar);
        vista.alAsignarRol(this::asignarRol);
        vista.alEliminar(this::eliminar);
        refrescar(null);
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    // CDU 004 - Buscar por DNI, correo y/o rol
    private void refrescar(String mensajeExito) {
        List<Usuario> encontrados = Collections.emptyList();
        try {
            encontrados = gestor.buscarUsuarios(admin, vacioANull(vista.getFiltroDni()),
                    vacioANull(vista.getFiltroCorreo()), vista.getFiltroRol());
        } catch (UsuarioNoEncontradoException e) {
            // tabla vacía
        } catch (UsuarioException e) {
            vista.mostrarError(e.getMessage());
        } catch (ErrorBaseDatosException e) {
            vista.mostrarError(e.getMessage());
        }
        vista.mostrarUsuarios(encontrados);
        if (mensajeExito != null) {
            vista.mostrarEstado(mensajeExito, true);
        } else if (encontrados.isEmpty()) {
            vista.mostrarEstado("Usuario no encontrado", false);
        } else {
            vista.mostrarEstado(encontrados.size() + " usuario(s)", null);
        }
    }

    // CDU 006 - Modificar usuario
    private void modificar() {
        Usuario usuario = vista.getSeleccionado();
        if (usuario == null) {
            return;
        }
        boolean ok = vista.editarUsuario(usuario, datos -> ejecutar(vista,
                () -> gestor.modificarUsuario(admin, usuario.getIdUsuario(), datos)));
        if (ok) {
            if (usuario.getIdUsuario() == admin.getIdUsuario()) {
                alCambiarDatosPropios.run();
            }
            refrescar("Modificación exitosa");
        }
    }

    // CDU 005 - Asignar rol
    private void asignarRol() {
        Usuario usuario = vista.getSeleccionado();
        if (usuario == null) {
            return;
        }
        boolean ok = vista.elegirRol(usuario, rol -> ejecutar(vista,
                () -> gestor.asignarRol(admin, usuario.getIdUsuario(), rol)));
        if (ok) {
            refrescar("El rol fue modificado correctamente");
        }
    }

    // CDU 004 - Eliminar
    private void eliminar() {
        Usuario usuario = vista.getSeleccionado();
        if (usuario == null) {
            return;
        }
        boolean confirmado = vista.confirmar("¿Eliminar a " + usuario.getNombre() + " " + usuario.getApellido()
                + " (" + usuario.getCorreoElectronico() + ")?\nEsta acción no se puede deshacer.", "Eliminar usuario");
        if (confirmado && ejecutar(vista, () -> gestor.eliminarUsuario(admin, usuario.getIdUsuario()))) {
            refrescar("Eliminación de usuario exitosa");
        }
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.trim().isEmpty() ? null : texto.trim();
    }
}
