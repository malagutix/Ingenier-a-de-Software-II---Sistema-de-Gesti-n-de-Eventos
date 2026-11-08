package modelo.eventos.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.TipoEvento;

/** Patrón Repository para tipos de evento. */
public interface TipoEventoRepository {
    void guardar(TipoEvento tipo);
    Optional<TipoEvento> buscarPorId(int id);
    List<TipoEvento> listarTodos();
    void actualizar(TipoEvento tipo);
    void eliminar(int id);
}
