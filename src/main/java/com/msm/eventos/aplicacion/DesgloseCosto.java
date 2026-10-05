package com.msm.eventos.aplicacion;

public record DesgloseCosto(double servicios, double recursos, double logistica) {
    public double total() {
        return servicios + recursos + logistica;
    }
}
