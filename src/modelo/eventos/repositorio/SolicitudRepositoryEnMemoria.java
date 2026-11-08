package modelo.eventos.repositorio;

import modelo.eventos.entidades.SolicitudEvento;

public class SolicitudRepositoryEnMemoria extends RepositorioEnMemoria<SolicitudEvento>
        implements SolicitudRepository {

    public SolicitudRepositoryEnMemoria() {
        super(SolicitudEvento::getIdSolicitud);
    }

    @Override
    protected void asignarId(SolicitudEvento solicitud, int id) {
        solicitud.setIdSolicitud(id);
    }
}
