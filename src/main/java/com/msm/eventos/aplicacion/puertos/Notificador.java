package com.msm.eventos.aplicacion.puertos;

import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;

/**
 * Contrato que implementa el Subsistema 3 (Notificaciones).
 */
public interface Notificador {
    void notificarAsignacion(AsignacionRecurso asignacion);

    void notificarReprogramacion(AsignacionRecurso asignacion, FranjaHoraria franjaAnterior);

    void notificarCancelacion(AsignacionRecurso asignacion);
}
