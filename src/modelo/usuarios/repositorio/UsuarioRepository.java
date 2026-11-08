package modelo.usuarios.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Usuario;

/**
 * Patrón Repository: separa la lógica de negocio del acceso a datos.
 * Si cambian de base de datos, solo se escribe otra implementación de esta interfaz.
 */
public interface UsuarioRepository {

    void guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(int id);

    Optional<Usuario> buscarPorDni(String dni);

    Optional<Usuario> buscarPorCorreo(String correo);

    List<Usuario> listarTodos();

    void actualizar(Usuario usuario);

    void eliminar(int id);

    /** Guarda que esas notificaciones del usuario ya fueron leídas. */
    void marcarNotificacionesLeidas(Usuario usuario, List<Notificacion> leidas);

    /** Borra esas notificaciones del usuario. */
    void eliminarNotificaciones(Usuario usuario, List<Notificacion> eliminadas);
}
