package modelo.eventos.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.Planificacion;

/** Patrón Repository para planificaciones generales. */
public interface PlanificacionRepository {
    void guardar(Planificacion planificacion);
    Optional<Planificacion> buscarPorId(int id);
    Optional<Planificacion> buscarPorSolicitud(int idSolicitud);
    List<Planificacion> listarTodos();
    void actualizar(Planificacion planificacion);
    void eliminar(int id);
}
