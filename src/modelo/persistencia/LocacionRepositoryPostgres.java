package modelo.persistencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.repositorio.LocacionRepository;

/** Repository de locaciones sobre PostgreSQL (tablas locaciones y bloqueos_locacion). */
public class LocacionRepositoryPostgres implements LocacionRepository {

    private final ContextoPersistencia ctx;

    public LocacionRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(Locacion l) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO locaciones (nombre, direccion, tipo,"
                    + " capacidad, distancia_km, tiempo_traslado_min, tiempo_montaje_min, vehiculos_requeridos,"
                    + " requiere_camara_frio, otros_requerimientos, habilitada)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id")) {
                completar(ps, l);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    l.setIdLocacion(rs.getInt(1));
                }
            }
            guardarBloqueos(c, l);
            ctx.locaciones.put(l.getIdLocacion(), l);
            return null;
        });
    }

    @Override
    public void actualizar(Locacion l) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE locaciones SET nombre = ?, direccion = ?, tipo = ?,"
                    + " capacidad = ?, distancia_km = ?, tiempo_traslado_min = ?, tiempo_montaje_min = ?,"
                    + " vehiculos_requeridos = ?, requiere_camara_frio = ?, otros_requerimientos = ?, habilitada = ?"
                    + " WHERE id = ?")) {
                completar(ps, l);
                ps.setInt(12, l.getIdLocacion());
                ps.executeUpdate();
            }
            guardarBloqueos(c, l);
            return null;
        });
    }

    @Override
    public void eliminar(int id) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM locaciones WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            ctx.locaciones.remove(id);
            return null;
        });
    }

    private static void completar(PreparedStatement ps, Locacion l) throws SQLException {
        ps.setString(1, l.getNombre());
        ps.setString(2, l.getDireccion());
        ps.setString(3, l.getTipo().name());
        ps.setInt(4, l.getCapacidad());
        ps.setBigDecimal(5, BigDecimal.valueOf(l.getDistanciaKm()));
        ps.setInt(6, l.getTiempoTrasladoMin());
        ps.setInt(7, l.getTiempoMontajeMin());
        ps.setInt(8, l.getVehiculosRequeridos());
        ps.setBoolean(9, l.isRequiereCamaraFrio());
        ps.setString(10, l.getOtrosRequerimientos());
        ps.setBoolean(11, l.isHabilitada());
    }

    /** Los bloqueos son parte de la locación: se reemplazan completos. */
    private static void guardarBloqueos(Connection c, Locacion l) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM bloqueos_locacion WHERE id_locacion = ?")) {
            ps.setInt(1, l.getIdLocacion());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO bloqueos_locacion (id_locacion, desde, hasta, motivo) VALUES (?, ?, ?, ?)")) {
            for (BloqueoFecha b : l.getBloqueos()) {
                ps.setInt(1, l.getIdLocacion());
                ps.setObject(2, b.getDesde());
                ps.setObject(3, b.getHasta());
                ps.setString(4, b.getMotivo());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Locacion> buscarPorId(int id) {
        ctx.asegurarActualizado();
        return Optional.ofNullable(ctx.locaciones.get(id));
    }

    @Override
    public List<Locacion> listarTodos() {
        ctx.asegurarActualizado();
        return new ArrayList<>(ctx.locaciones.values());
    }
}
