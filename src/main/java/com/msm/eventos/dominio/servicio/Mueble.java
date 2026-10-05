package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

public record Mueble(String descripcion, int cantidad, double precioUnitario) {
    public Mueble {
        Validar.texto(descripcion, "descripcion");
        Validar.noNegativo(cantidad, "cantidad");
        Validar.noNegativo(precioUnitario, "precioUnitario");
    }

    public double subtotal() {
        return cantidad * precioUnitario;
    }
}
