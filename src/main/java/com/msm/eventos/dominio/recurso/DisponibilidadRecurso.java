package com.msm.eventos.dominio.recurso;

import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;

public class DisponibilidadRecurso implements Identificable {
    private final String id;
    private final RecursoAsignable recurso;
    private final FranjaHoraria franja;

    public DisponibilidadRecurso(RecursoAsignable recurso, FranjaHoraria franja) {
        this.id = GeneradorCodigo.siguiente("DSP");
        this.recurso = Validar.requerido(recurso, "recurso");
        this.franja = Validar.requerido(franja, "franja");
    }

    public boolean cubre(FranjaHoraria solicitada) {
        return franja.contiene(solicitada);
    }

    @Override
    public String getId() {
        return id;
    }

    public RecursoAsignable getRecurso() {
        return recurso;
    }

    public FranjaHoraria getFranja() {
        return franja;
    }
}
