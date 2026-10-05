package com.msm.eventos.dominio.evento;

import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;
import com.msm.eventos.dominio.recurso.Locacion;
import com.msm.eventos.dominio.servicio.Servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * Punto de integración con el Subsistema 1 (Eventos y Planificación), que es quien
 * crea la hoja. El Subsistema 2 le agrega servicios, asignaciones y calcula su costo.
 */
public class HojaServicio implements Identificable {
    private final String id;
    private final String idEvento;
    private int cantidadInvitados;
    private FranjaHoraria franja;
    private Locacion locacion;
    private final List<Servicio> servicios = new ArrayList<>();
    private final List<AsignacionRecurso> asignaciones = new ArrayList<>();

    public HojaServicio(String idEvento, int cantidadInvitados, FranjaHoraria franja, Locacion locacion) {
        this.id = GeneradorCodigo.siguiente("HOJ");
        this.idEvento = Validar.texto(idEvento, "idEvento");
        this.cantidadInvitados = Validar.noNegativo(cantidadInvitados, "cantidadInvitados");
        this.franja = Validar.requerido(franja, "franja");
        this.locacion = locacion;
    }

    public void agregarServicio(Servicio servicio) {
        servicios.add(Validar.requerido(servicio, "servicio"));
    }

    public void quitarServicio(Servicio servicio) {
        servicios.remove(servicio);
    }

    public void agregarAsignacion(AsignacionRecurso asignacion) {
        asignaciones.add(Validar.requerido(asignacion, "asignacion"));
    }

    public void quitarAsignacion(AsignacionRecurso asignacion) {
        asignaciones.remove(asignacion);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public int getCantidadInvitados() {
        return cantidadInvitados;
    }

    public void setCantidadInvitados(int cantidadInvitados) {
        this.cantidadInvitados = Validar.noNegativo(cantidadInvitados, "cantidadInvitados");
    }

    public FranjaHoraria getFranja() {
        return franja;
    }

    public void setFranja(FranjaHoraria franja) {
        this.franja = Validar.requerido(franja, "franja");
    }

    public Locacion getLocacion() {
        return locacion;
    }

    public void setLocacion(Locacion locacion) {
        this.locacion = locacion;
    }

    public List<Servicio> getServicios() {
        return List.copyOf(servicios);
    }

    public List<AsignacionRecurso> getAsignaciones() {
        return List.copyOf(asignaciones);
    }
}
