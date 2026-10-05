package com.msm.eventos.dominio.recurso;

import com.msm.eventos.dominio.comun.Identificable;

public interface RecursoAsignable extends Identificable {
    String getDescripcion();

    double getCostoPorHora();

    boolean estaActivo();
}
