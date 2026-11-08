package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.Evento;
import modelo.eventos.repositorio.EventoRepository;

/** Repository de eventos sobre PostgreSQL. El estado (patrón State) se guarda por su nombre. */
public class EventoRepositoryPostgres implements EventoRepository {

    private final ContextoPersistencia ctx;

    public EventoRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(Evento e) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO eventos (fecha, hora_inicio, duracion_horas,"
                    + " cantidad_asistentes, requerimientos_especificos, id_tipo_evento, id_locacion, estado, fue_aceptado,"
                    + " id_planificacion, id_organizador) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
                completar(ps, e);
                ps.setInt(10, e.getPlanificacion().getIdPlanificacion());
                ps.setInt(11, e.getOrganizador().getIdUsuario());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    e.setIdEvento(rs.getInt(1));
                }
            }
            ctx.eventos.put(e.getIdEvento(), e);
            return null;
        });
    }

    @Override
    public void actualizar(Evento e) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE eventos SET fecha = ?, hora_inicio = ?,"
                    + " duracion_horas = ?, cantidad_asistentes = ?, requerimientos_especificos = ?,"
                    + " id_tipo_evento = ?, id_locacion = ?, estado = ?, fue_aceptado = ? WHERE id = ?")) {
                completar(ps, e);
                ps.setInt(10, e.getIdEvento());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM eventos WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.eventos.remove(id);
            return null;
        });
    }

    private static void completar(PreparedStatement ps, Evento e) throws SQLException {
        ps.setObject(1, e.getFecha());
        ps.setObject(2, e.getHoraInicio());
        ps.setInt(3, e.getDuracionHoras());
        ps.setInt(4, e.getCantidadAsistentes());
        ps.setString(5, e.getRequerimientosEspecificos() == null ? "" : e.getRequerimientosEspecificos());
        ps.setInt(6, e.getTipo().getIdTipoEvento());
        ps.setInt(7, e.getLocacion().getIdLocacion());
        ps.setString(8, e.getEstado().getNombre());
        ps.setBoolean(9, e.fueAceptado());
    }

    @Override
    public Optional<Evento> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.eventos.get(id));
    }

    @Override
    public List<Evento> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.eventos.values());
    }
}
