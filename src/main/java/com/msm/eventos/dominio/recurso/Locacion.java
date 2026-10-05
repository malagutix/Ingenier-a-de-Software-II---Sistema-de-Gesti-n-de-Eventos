package com.msm.eventos.dominio.recurso;

import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.servicio.Servicio;

import java.util.ArrayList;
import java.util.List;

/**
 * La gestión completa de locaciones pertenece al Subsistema 1; aquí se modela
 * únicamente como recurso asignable y fuente de costos logísticos.
 */
public class Locacion implements RecursoAsignable {
    private final String id;
    private String nombre;
    private String direccion;
    private int capacidad;
    private double distanciaSedeKm;
    private int tiempoTrasladoMin;
    private String requerimientosLogisticos;
    private double costoPorHora;
    private boolean activo = true;
    private final List<Servicio> serviciosIncluidos = new ArrayList<>();

    public Locacion(String nombre, String direccion, int capacidad, double distanciaSedeKm,
                    int tiempoTrasladoMin, double costoPorHora) {
        this.id = GeneradorCodigo.siguiente("LOC");
        this.nombre = Validar.texto(nombre, "nombre");
        this.direccion = Validar.texto(direccion, "direccion");
        this.capacidad = Validar.noNegativo(capacidad, "capacidad");
        this.distanciaSedeKm = Validar.noNegativo(distanciaSedeKm, "distanciaSedeKm");
        this.tiempoTrasladoMin = Validar.noNegativo(tiempoTrasladoMin, "tiempoTrasladoMin");
        this.costoPorHora = Validar.noNegativo(costoPorHora, "costoPorHora");
    }

    public void incluirServicio(Servicio servicio) {
        serviciosIncluidos.add(Validar.requerido(servicio, "servicio"));
    }

    public void darDeBaja() {
        activo = false;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescripcion() {
        return nombre + " - " + direccion;
    }

    @Override
    public double getCostoPorHora() {
        return costoPorHora;
    }

    @Override
    public boolean estaActivo() {
        return activo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public double getDistanciaSedeKm() {
        return distanciaSedeKm;
    }

    public int getTiempoTrasladoMin() {
        return tiempoTrasladoMin;
    }

    public String getRequerimientosLogisticos() {
        return requerimientosLogisticos;
    }

    public void setRequerimientosLogisticos(String requerimientosLogisticos) {
        this.requerimientosLogisticos = requerimientosLogisticos;
    }

    public List<Servicio> getServiciosIncluidos() {
        return List.copyOf(serviciosIncluidos);
    }
}
