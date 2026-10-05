package com.msm.eventos.infraestructura.notificacion;

import com.msm.eventos.aplicacion.puertos.Notificador;
import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;

/**
 * Adaptador provisorio hasta integrar el Subsistema 3.
 */
public class NotificadorConsola implements Notificador {

    @Override
    public void notificarAsignacion(AsignacionRecurso a) {
        System.out.printf("[NOTIFICACIÓN] %s asignado a %s (%s)%n",
                a.getRecurso().getDescripcion(), a.getIdHojaServicio(), a.getFranja());
    }

    @Override
    public void notificarReprogramacion(AsignacionRecurso a, FranjaHoraria anterior) {
        System.out.printf("[NOTIFICACIÓN] %s reprogramado: %s -> %s%n",
                a.getRecurso().getDescripcion(), anterior, a.getFranja());
    }

    @Override
    public void notificarCancelacion(AsignacionRecurso a) {
        System.out.printf("[NOTIFICACIÓN] Asignación cancelada: %s en %s%n",
                a.getRecurso().getDescripcion(), a.getIdHojaServicio());
    }
}
