package modelo.usuarios.repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Usuario;

/**
 * Implementación en memoria del repositorio. Sirve para desarrollar y probar
 * sin base de datos; después se reemplaza por una implementación JDBC/JPA
 * sin tocar GestorUsuarios.
 */
public class UsuarioRepositoryEnMemoria implements UsuarioRepository {

    private final Map<Integer, Usuario> usuarios = new LinkedHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(1);

    @Override
    public synchronized void guardar(Usuario usuario) {
        usuario.setIdUsuario(secuencia.getAndIncrement());
        usuarios.put(usuario.getIdUsuario(), usuario);
    }

    @Override
    public synchronized Optional<Usuario> buscarPorId(int id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    @Override
    public synchronized Optional<Usuario> buscarPorDni(String dni) {
        return usuarios.values().stream()
                .filter(u -> u.getDni().equals(dni))
                .findFirst();
    }

    @Override
    public synchronized Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarios.values().stream()
                .filter(u -> u.getCorreoElectronico().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public synchronized List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios.values());
    }

    @Override
    public synchronized void actualizar(Usuario usuario) {
        usuarios.put(usuario.getIdUsuario(), usuario);
    }

    @Override
    public synchronized void eliminar(int id) {
        usuarios.remove(id);
    }

    // En memoria la entidad ya quedó modificada: no hay nada más que guardar
    @Override
    public void marcarNotificacionesLeidas(Usuario usuario, List<Notificacion> leidas) {
    }

    @Override
    public void eliminarNotificaciones(Usuario usuario, List<Notificacion> eliminadas) {
    }
}
