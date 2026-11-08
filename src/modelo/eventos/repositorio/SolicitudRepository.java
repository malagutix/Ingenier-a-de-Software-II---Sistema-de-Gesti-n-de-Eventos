package modelo.eventos.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.SolicitudEvento;

/** Patrón Repository para solicitudes. */
public interface SolicitudRepository {
    void guardar(SolicitudEvento solicitud);
    Optional<SolicitudEvento> buscarPorId(int id);
    List<SolicitudEvento> listarTodos();
    void actualizar(SolicitudEvento solicitud);
    void eliminar(int id);
}
