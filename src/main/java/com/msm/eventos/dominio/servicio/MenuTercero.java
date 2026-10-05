package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

public class MenuTercero extends Menu {
    private double precioPorPersona;

    public MenuTercero(String nombre, String descripcion, int minimoComensales, double precioPorPersona) {
        super(nombre, descripcion, minimoComensales);
        setPrecioPorPersona(precioPorPersona);
    }

    @Override
    public double costoPorPersona() {
        return precioPorPersona;
    }

    public void setPrecioPorPersona(double precioPorPersona) {
        this.precioPorPersona = Validar.noNegativo(precioPorPersona, "precioPorPersona");
    }
}
