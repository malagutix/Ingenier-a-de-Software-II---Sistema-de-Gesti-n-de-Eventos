package com.msm.eventos.dominio.comun;

public final class Validar {
    private Validar() {
    }

    public static String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    public static double noNegativo(double valor, String campo) {
        if (valor < 0) {
            throw new ReglaNegocioException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor;
    }

    public static int noNegativo(int valor, String campo) {
        if (valor < 0) {
            throw new ReglaNegocioException("El campo '" + campo + "' no puede ser negativo");
        }
        return valor;
    }

    public static <T> T requerido(T valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio");
        }
        return valor;
    }
}
