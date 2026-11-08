package modelo.eventos.repositorio;

import modelo.eventos.entidades.TipoEvento;

public class TipoEventoRepositoryEnMemoria extends RepositorioEnMemoria<TipoEvento> implements TipoEventoRepository {

    public TipoEventoRepositoryEnMemoria() {
        super(TipoEvento::getIdTipoEvento);
    }

    @Override
    protected void asignarId(TipoEvento tipo, int id) {
        tipo.setIdTipoEvento(id);
    }
}
