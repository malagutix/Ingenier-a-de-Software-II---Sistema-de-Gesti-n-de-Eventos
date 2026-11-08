package modelo.eventos.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.Locacion;

/** Patrón Repository para locaciones. */
public interface LocacionRepository {
    void guardar(Locacion locacion);
    Optional<Locacion> buscarPorId(int id);
    List<Locacion> listarTodos();
    void actualizar(Locacion locacion);
    void eliminar(int id);
}
