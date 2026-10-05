package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

public class Entretenimiento extends Servicio {
    private CategoriaEntretenimiento categoria;
    private double costoPorHora;

    public Entretenimiento(String nombre, String descripcion, CategoriaEntretenimiento categoria,
                           double costoBase, double costoPorHora) {
        super(nombre, descripcion, costoBase);
        setCategoria(categoria);
        setCostoPorHora(costoPorHora);
    }

    @Override
    protected double costoVariable(ParametrosCosto parametros) {
        return costoPorHora * parametros.horas();
    }

    public CategoriaEntretenimiento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEntretenimiento categoria) {
        this.categoria = Validar.requerido(categoria, "categoria");
    }

    public double getCostoPorHora() {
        return costoPorHora;
    }

    public void setCostoPorHora(double costoPorHora) {
        this.costoPorHora = Validar.noNegativo(costoPorHora, "costoPorHora");
    }
}
