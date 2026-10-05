package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

import java.util.ArrayList;
import java.util.List;

public class MenuPropio extends Menu {
    private final List<Plato> platos = new ArrayList<>();
    private double margen;
    private double adicionalPorPersona;

    public MenuPropio(String nombre, String descripcion, int minimoComensales, double margen, double adicionalPorPersona) {
        super(nombre, descripcion, minimoComensales);
        setMargen(margen);
        setAdicionalPorPersona(adicionalPorPersona);
    }

    @Override
    public double costoPorPersona() {
        double ingredientes = platos.stream().mapToDouble(Plato::costoPorPorcion).sum();
        return ingredientes * (1 + margen) + adicionalPorPersona;
    }

    public void agregarPlato(Plato plato) {
        platos.add(Validar.requerido(plato, "plato"));
    }

    public void quitarPlato(Plato plato) {
        platos.remove(plato);
    }

    public List<Plato> getPlatos() {
        return List.copyOf(platos);
    }

    public double getMargen() {
        return margen;
    }

    public void setMargen(double margen) {
        this.margen = Validar.noNegativo(margen, "margen");
    }

    public double getAdicionalPorPersona() {
        return adicionalPorPersona;
    }

    public void setAdicionalPorPersona(double adicionalPorPersona) {
        this.adicionalPorPersona = Validar.noNegativo(adicionalPorPersona, "adicionalPorPersona");
    }
}
