package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.Planificacion;
import modelo.eventos.repositorio.PlanificacionRepository;

/**
 * Repository de planificaciones sobre PostgreSQL. Los eventos de cada
 * planificación se guardan en la tabla eventos (clave id_planificacion).
 */
public class PlanificacionRepositoryPostgres implements PlanificacionRepository {

    private final ContextoPersistencia ctx;

    public PlanificacionRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(Planificacion p) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO planificaciones (nombre, id_solicitud) VALUES (?, ?) RETURNING id")) {
                ps.setString(1, p.getNombre());
                ps.setInt(2, p.getSolicitud().getIdSolicitud());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    p.setIdPlanificacion(rs.getInt(1));
                }
            }
            ctx.planificaciones.put(p.getIdPlanificacion(), p);
            return null;
        });
    }

    @Override
    public void actualizar(Planificacion p) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE planificaciones SET nombre = ? WHERE id = ?")) {
                ps.setString(1, p.getNombre());
                ps.setInt(2, p.getIdPlanificacion());
                ps.executeUpdate();
            }
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM planificaciones WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.planificaciones.remove(id);
            return null;
        });
    }

    @Override
    public Optional<Planificacion> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.planificaciones.get(id));
    }

    @Override
    public Optional<Planificacion> buscarPorSolicitud(int idSolicitud) {
        ctx.asegurarActualizado();
        return ctx.planificaciones.values().stream()
                .filter(p -> p.getSolicitud().getIdSolicitud() == idSolicitud)
                .findFirst();
    }

    @Override
    public List<Planificacion> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.planificaciones.values());
    }
}
