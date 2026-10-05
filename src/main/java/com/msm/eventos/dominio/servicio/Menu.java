package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public abstract class Menu extends Servicio {
    private final Set<TipoDieta> dietas = EnumSet.noneOf(TipoDieta.class);
    private int minimoComensales;

    protected Menu(String nombre, String descripcion, int minimoComensales) {
        super(nombre, descripcion, 0);
        setMinimoComensales(minimoComensales);
    }

    public abstract double costoPorPersona();

    @Override
    protected double costoVariable(ParametrosCosto parametros) {
        return costoPorPersona() * parametros.cantidadPersonas();
    }

    @Override
    public int getMinimoPersonas() {
        return minimoComensales;
    }

    public void clasificar(TipoDieta... tipos) {
        dietas.addAll(Arrays.asList(tipos));
    }

    public void desclasificar(TipoDieta tipo) {
        dietas.remove(tipo);
    }

    public boolean esApto(TipoDieta tipo) {
        return dietas.contains(tipo);
    }

    public Set<TipoDieta> getDietas() {
        return Set.copyOf(dietas);
    }

    public void setMinimoComensales(int minimoComensales) {
        this.minimoComensales = Validar.noNegativo(minimoComensales, "minimoComensales");
    }
}
