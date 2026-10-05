package com.msm.eventos.dominio.recurso;

import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;

import java.util.Optional;

public class AsignacionRecurso implements Identificable {
    private final String id;
    private final RecursoAsignable recurso;
    private final String idHojaServicio;
    private FranjaHoraria franja;
    private Boolean asistio;

    public AsignacionRecurso(RecursoAsignable recurso, FranjaHoraria franja, String idHojaServicio) {
        this.id = GeneradorCodigo.siguiente("ASG");
        this.recurso = Validar.requerido(recurso, "recurso");
        this.franja = Validar.requerido(franja, "franja");
        this.idHojaServicio = Validar.texto(idHojaServicio, "idHojaServicio");
    }

    public double costo() {
        return recurso.getCostoPorHora() * franja.horas();
    }

    public void reprogramar(FranjaHoraria nuevaFranja) {
        this.franja = Validar.requerido(nuevaFranja, "franja");
    }

    public void registrarAsistencia(boolean asistio) {
        this.asistio = asistio;
    }

    @Override
    public String getId() {
        return id;
    }

    public RecursoAsignable getRecurso() {
        return recurso;
    }

    public String getIdHojaServicio() {
        return idHojaServicio;
    }

    public FranjaHoraria getFranja() {
        return franja;
    }

    public Optional<Boolean> getAsistio() {
        return Optional.ofNullable(asistio);
    }
}
