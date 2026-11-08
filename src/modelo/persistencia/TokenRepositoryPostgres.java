package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Optional;

import modelo.usuarios.entidades.TokenRecuperacion;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.repositorio.TokenRepository;

/** Códigos de recuperación de contraseña guardados en PostgreSQL (sirven aunque se reinicie la aplicación). */
public class TokenRepositoryPostgres implements TokenRepository {

    private final ContextoPersistencia ctx;

    public TokenRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(TokenRecuperacion t) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO tokens_recuperacion (codigo, id_usuario, vence, usado) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, t.getCodigo());
                ps.setInt(2, t.getUsuario().getIdUsuario());
                ps.setObject(3, t.getFechaExpiracion());
                ps.setBoolean(4, t.isUsado());
                ps.executeUpdate();
            }
            // se aprovecha para borrar los vencidos
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM tokens_recuperacion WHERE vence < ?")) {
                ps.setObject(1, LocalDateTime.now().minusDays(1));
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorCodigo(String codigo) {
        ctx.asegurarActualizado();
        return ctx.leer(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id_usuario, vence, usado FROM tokens_recuperacion WHERE codigo = ?")) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.<TokenRecuperacion>empty();
                    }
                    Usuario usuario = ctx.usuarios.get(rs.getInt(1));
                    if (usuario == null) {
                        return Optional.<TokenRecuperacion>empty();
                    }
                    return Optional.of(new TokenRecuperacion(codigo, usuario,
                            rs.getObject(2, LocalDateTime.class), rs.getBoolean(3)));
                }
            }
        });
    }

    @Override
    public void actualizar(TokenRecuperacion t) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE tokens_recuperacion SET usado = ? WHERE codigo = ?")) {
                ps.setBoolean(1, t.isUsado());
                ps.setString(2, t.getCodigo());
                ps.executeUpdate();
            }
            return null;
        });
    }
}
