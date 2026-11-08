package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.repositorio.UsuarioRepository;

/** Repository de usuarios sobre PostgreSQL (tabla usuarios). */
public class UsuarioRepositoryPostgres implements UsuarioRepository {

    private final ContextoPersistencia ctx;

    public UsuarioRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(Usuario u) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO usuarios"
                    + " (nombre, apellido, dni, correo, contrasena_hash, rol) VALUES (?, ?, ?, ?, ?, ?) RETURNING id")) {
                completar(ps, u);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    u.setIdUsuario(rs.getInt(1));
                }
            }
            ctx.registrarUsuarioNuevo(u);
            return null;
        });
    }

    @Override
    public void actualizar(Usuario u) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE usuarios SET nombre = ?, apellido = ?, dni = ?,"
                    + " correo = ?, contrasena_hash = ?, rol = ? WHERE id = ?")) {
                completar(ps, u);
                ps.setInt(7, u.getIdUsuario());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.quitarUsuario(id);
            return null;
        });
    }

    private static void completar(PreparedStatement ps, Usuario u) throws java.sql.SQLException {
        ps.setString(1, u.getNombre());
        ps.setString(2, u.getApellido());
        ps.setString(3, u.getDni());
        ps.setString(4, u.getCorreoElectronico());
        ps.setString(5, u.getContrasenaHash());
        ps.setString(6, u.getRol().getNombre());
    }

    @Override
    public synchronized Optional<Usuario> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.usuarios.get(id));
    }

    @Override
    public Optional<Usuario> buscarPorDni(String dni) {
        return listarTodos().stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return listarTodos().stream().filter(u -> u.getCorreoElectronico().equalsIgnoreCase(correo)).findFirst();
    }

    @Override
    public List<Usuario> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.usuarios.values());
    }

    @Override
    public void marcarNotificacionesLeidas(Usuario usuario, List<Notificacion> leidas) {
        ejecutarSobre("UPDATE notificaciones SET leida = true WHERE id_usuario = ? AND id = ANY (?)", usuario, leidas);
    }

    @Override
    public void eliminarNotificaciones(Usuario usuario, List<Notificacion> eliminadas) {
        ejecutarSobre("DELETE FROM notificaciones WHERE id_usuario = ? AND id = ANY (?)", usuario, eliminadas);
    }

    private void ejecutarSobre(String sql, Usuario usuario, List<Notificacion> notificaciones) {
        Integer[] ids = notificaciones.stream().filter(n -> n.getIdNotificacion() != 0)
                .map(Notificacion::getIdNotificacion).toArray(Integer[]::new);
        if (ids.length == 0) {
            return;
        }
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, usuario.getIdUsuario());
                ps.setArray(2, c.createArrayOf("integer", ids));
                ps.executeUpdate();
            }
            return null;
        });
    }
}
