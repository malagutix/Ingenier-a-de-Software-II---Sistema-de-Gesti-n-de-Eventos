package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.repositorio.TipoEventoRepository;

/** Repository de tipos de evento sobre PostgreSQL. */
public class TipoEventoRepositoryPostgres implements TipoEventoRepository {

    private final ContextoPersistencia ctx;

    public TipoEventoRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(TipoEvento t) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO tipos_evento (nombre, duracion_sugerida,"
                    + " hora_inicio_sugerida, requerimientos_sugeridos, activo) VALUES (?, ?, ?, ?, ?) RETURNING id")) {
                completar(ps, t);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    t.setIdTipoEvento(rs.getInt(1));
                }
            }
            ctx.tipos.put(t.getIdTipoEvento(), t);
            return null;
        });
    }

    @Override
    public void actualizar(TipoEvento t) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE tipos_evento SET nombre = ?, duracion_sugerida = ?,"
                    + " hora_inicio_sugerida = ?, requerimientos_sugeridos = ?, activo = ? WHERE id = ?")) {
                completar(ps, t);
                ps.setInt(6, t.getIdTipoEvento());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM tipos_evento WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.tipos.remove(id);
            return null;
        });
    }

    private static void completar(PreparedStatement ps, TipoEvento t) throws SQLException {
        ps.setString(1, t.getNombre());
        ps.setInt(2, t.getDuracionSugerida());
        ps.setObject(3, t.getHoraInicioSugerida());
        ps.setString(4, t.getRequerimientosSugeridos());
        ps.setBoolean(5, t.isActivo());
    }

    @Override
    public Optional<TipoEvento> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.tipos.get(id));
    }

    @Override
    public List<TipoEvento> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.tipos.values());
    }
}
