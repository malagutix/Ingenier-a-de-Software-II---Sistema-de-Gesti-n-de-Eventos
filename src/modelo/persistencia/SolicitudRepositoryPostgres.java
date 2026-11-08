package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.repositorio.SolicitudRepository;

/** Repository de solicitudes sobre PostgreSQL. */
public class SolicitudRepositoryPostgres implements SolicitudRepository {

    private final ContextoPersistencia ctx;

    public SolicitudRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(SolicitudEvento s) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO solicitudes (id_cliente, fecha_solicitada,"
                    + " id_tipo_evento, cantidad_invitados, id_locacion, menu, estado)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
                completar(ps, s);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    s.setIdSolicitud(rs.getInt(1));
                }
            }
            ctx.solicitudes.put(s.getIdSolicitud(), s);
            return null;
        });
    }

    @Override
    public void actualizar(SolicitudEvento s) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE solicitudes SET id_cliente = ?,"
                    + " fecha_solicitada = ?, id_tipo_evento = ?, cantidad_invitados = ?, id_locacion = ?, menu = ?,"
                    + " estado = ? WHERE id = ?")) {
                completar(ps, s);
                ps.setInt(8, s.getIdSolicitud());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM solicitudes WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.solicitudes.remove(id);
            return null;
        });
    }

    private static void completar(PreparedStatement ps, SolicitudEvento s) throws SQLException {
        ps.setInt(1, s.getCliente().getIdUsuario());
        ps.setObject(2, s.getFechaSolicitada());
        ps.setInt(3, s.getTipoEvento().getIdTipoEvento());
        ps.setInt(4, s.getCantidadInvitados());
        ps.setInt(5, s.getLocacion().getIdLocacion());
        ps.setString(6, s.getMenu().name());
        ps.setString(7, s.getEstado().name());
    }

    @Override
    public Optional<SolicitudEvento> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.solicitudes.get(id));
    }

    @Override
    public List<SolicitudEvento> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.solicitudes.values());
    }
}
