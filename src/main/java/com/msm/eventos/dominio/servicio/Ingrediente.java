package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

public record Ingrediente(String nombre, double costoPorPorcion) {
    public Ingrediente {
        Validar.texto(nombre, "nombre");
        Validar.noNegativo(costoPorPorcion, "costoPorPorcion");
    }
}
