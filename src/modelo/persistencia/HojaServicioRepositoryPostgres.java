package modelo.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import modelo.eventos.entidades.HojaServicio;
import modelo.eventos.repositorio.HojaServicioRepository;

/** Guarda un registro de cada hoja de servicio generada, con su contenido. */
public class HojaServicioRepositoryPostgres implements HojaServicioRepository {

    private final ContextoPersistencia ctx;

    public HojaServicioRepositoryPostgres(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    @Override
    public void guardar(HojaServicio hoja) {
        ctx.escribir(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO hojas_servicio (id_evento, generada, contenido)"
                    + " VALUES (?, ?, ?) RETURNING id")) {
                ps.setInt(1, hoja.getIdEvento());
                ps.setObject(2, hoja.getFechaGeneracion());
                ps.setString(3, "");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    hoja.setIdHoja(rs.getInt(1));
                }
            }
            // el contenido incluye el número de hoja, por eso se guarda después de obtenerlo
            try (PreparedStatement ps = c.prepareStatement("UPDATE hojas_servicio SET contenido = ? WHERE id = ?")) {
                ps.setString(1, hoja.getDetalle());
                ps.setInt(2, hoja.getIdHoja());
                ps.executeUpdate();
            }
            return null;
        });
    }
}
