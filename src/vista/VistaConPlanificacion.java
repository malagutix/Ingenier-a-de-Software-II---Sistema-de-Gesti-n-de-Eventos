package vista;

import java.util.List;
import java.util.function.Predicate;

import vista.DialogoPlanificar.OpcionSolicitud;
import vista.DialogoPlanificar.Pedido;

/** Vista desde la que se pueden planificar eventos (Planificaciones y Eventos). */
public interface VistaConPlanificacion extends Vista {

    /**
     * Muestra el diálogo de planificación. Se repite hasta que el controlador
     * acepte el pedido o se cancele.
     *
     * @param idSolicitud solicitud a preseleccionar, o null
     */
    default boolean pedirPlanificacion(List<OpcionSolicitud> opciones, Integer idSolicitud,
                                       Predicate<Pedido> alAceptar) {
        return DialogoPlanificar.mostrar(componente(), opciones, idSolicitud, alAceptar);
    }
}
