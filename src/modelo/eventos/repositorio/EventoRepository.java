package modelo.eventos.repositorio;

import java.util.List;
import java.util.Optional;

import modelo.eventos.entidades.Evento;

/** Patrón Repository para eventos. */
public interface EventoRepository {
    void guardar(Evento evento);
    Optional<Evento> buscarPorId(int id);
    List<Evento> listarTodos();
    void actualizar(Evento evento);
    void eliminar(int id);
}
