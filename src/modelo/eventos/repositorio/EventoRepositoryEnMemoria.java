package modelo.eventos.repositorio;

import modelo.eventos.entidades.Evento;

public class EventoRepositoryEnMemoria extends RepositorioEnMemoria<Evento> implements EventoRepository {

    public EventoRepositoryEnMemoria() {
        super(Evento::getIdEvento);
    }

    @Override
    protected void asignarId(Evento evento, int id) {
        evento.setIdEvento(id);
    }
}
