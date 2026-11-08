package modelo.eventos.repositorio;

import java.util.Optional;

import modelo.eventos.entidades.Planificacion;

public class PlanificacionRepositoryEnMemoria extends RepositorioEnMemoria<Planificacion>
        implements PlanificacionRepository {

    public PlanificacionRepositoryEnMemoria() {
        super(Planificacion::getIdPlanificacion);
    }

    @Override
    protected void asignarId(Planificacion planificacion, int id) {
        planificacion.setIdPlanificacion(id);
    }

    @Override
    public Optional<Planificacion> buscarPorSolicitud(int idSolicitud) {
        return listarTodos().stream()
                .filter(p -> p.getSolicitud().getIdSolicitud() == idSolicitud)
                .findFirst();
    }
}
