package com.msm.eventos.aplicacion;

import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.evento.HojaServicio;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;
import com.msm.eventos.dominio.servicio.ParametrosCosto;

/**
 * El costo se calcula siempre sobre el estado actual de la hoja, por lo que cualquier
 * cambio en servicios, menús o asignaciones se refleja al recalcular (RF 022).
 */
public class CalculadorCostoEvento {
    private final double costoPorKm;

    public CalculadorCostoEvento(double costoPorKm) {
        this.costoPorKm = Validar.noNegativo(costoPorKm, "costoPorKm");
    }

    public DesgloseCosto calcular(HojaServicio hoja) {
        ParametrosCosto parametros = new ParametrosCosto(hoja.getCantidadInvitados(), hoja.getFranja().horas());
        double servicios = hoja.getServicios().stream().mapToDouble(s -> s.calcularCosto(parametros)).sum();
        double recursos = hoja.getAsignaciones().stream().mapToDouble(AsignacionRecurso::costo).sum();
        double logistica = hoja.getLocacion() == null ? 0 : hoja.getLocacion().getDistanciaSedeKm() * 2 * costoPorKm;
        return new DesgloseCosto(servicios, recursos, logistica);
    }
}
