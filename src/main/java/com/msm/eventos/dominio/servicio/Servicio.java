package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.proveedor.Proveedor;

import java.util.ArrayList;
import java.util.List;

/**
 * Componente del patrón Composite. Las hojas son los servicios concretos
 * (menús, entretenimiento, mobiliario) y {@link ServicioCompuesto} es el compuesto.
 */
public abstract class Servicio implements Identificable {
    private final String id;
    private String nombre;
    private String descripcion;
    private double costoBase;
    private Proveedor proveedor;
    private boolean activo = true;
    private final List<RequerimientoPersonal> requerimientosPersonal = new ArrayList<>();

    protected Servicio(String nombre, String descripcion, double costoBase) {
        this.id = GeneradorCodigo.siguiente("SRV");
        setNombre(nombre);
        setDescripcion(descripcion);
        setCostoBase(costoBase);
    }

    public final double calcularCosto(ParametrosCosto parametros) {
        return costoBase + costoVariable(parametros);
    }

    protected abstract double costoVariable(ParametrosCosto parametros);

    public boolean estaDisponible() {
        return activo;
    }

    public int getMinimoPersonas() {
        return 0;
    }

    public List<RequerimientoPersonal> getRequerimientosPersonal() {
        return List.copyOf(requerimientosPersonal);
    }

    public void agregarRequerimiento(RequerimientoPersonal requerimiento) {
        requerimientosPersonal.add(Validar.requerido(requerimiento, "requerimiento"));
    }

    public boolean esCompuesto() {
        return false;
    }

    public List<Servicio> getComponentes() {
        return List.of();
    }

    public boolean contiene(Servicio otro) {
        return false;
    }

    public String getResumen() {
        return resumen("");
    }

    protected String resumen(String sangria) {
        return String.format("%s- %s [%s]", sangria, nombre, getClass().getSimpleName());
    }

    public void darDeBaja() {
        activo = false;
    }

    public void reactivar() {
        activo = true;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validar.texto(nombre, "nombre");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion == null ? "" : descripcion.trim();
    }

    public double getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(double costoBase) {
        this.costoBase = Validar.noNegativo(costoBase, "costoBase");
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public boolean estaActivo() {
        return activo;
    }
}
