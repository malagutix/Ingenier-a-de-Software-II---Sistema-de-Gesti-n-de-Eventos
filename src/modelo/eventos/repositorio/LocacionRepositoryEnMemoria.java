package modelo.eventos.repositorio;

import modelo.eventos.entidades.Locacion;

public class LocacionRepositoryEnMemoria extends RepositorioEnMemoria<Locacion> implements LocacionRepository {

    public LocacionRepositoryEnMemoria() {
        super(Locacion::getIdLocacion);
    }

    @Override
    protected void asignarId(Locacion locacion, int id) {
        locacion.setIdLocacion(id);
    }
}
