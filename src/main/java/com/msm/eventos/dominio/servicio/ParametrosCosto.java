package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

public record ParametrosCosto(int cantidadPersonas, double horas) {
    public ParametrosCosto {
        Validar.noNegativo(cantidadPersonas, "cantidadPersonas");
        Validar.noNegativo(horas, "horas");
    }
}
