package com.msm.eventos.dominio.proveedor;

import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.servicio.Servicio;

import java.util.ArrayList;
import java.util.List;

public class Proveedor implements Identificable {
    private final String id;
    private String nombreLegal;
    private String cuit;
    private String rubro;
    private String email;
    private String telefono;
    private boolean propio;
    private boolean activo = true;
    private final List<Servicio> servicios = new ArrayList<>();

    public Proveedor(String nombreLegal, String cuit, String rubro, String email, boolean propio) {
        this.id = GeneradorCodigo.siguiente("PRV");
        setNombreLegal(nombreLegal);
        setCuit(cuit);
        setRubro(rubro);
        setEmail(email);
        this.propio = propio;
    }

    public void agregarServicio(Servicio servicio) {
        Validar.requerido(servicio, "servicio");
        if (!activo) {
            throw new ReglaNegocioException("No se pueden agregar servicios a un proveedor dado de baja");
        }
        servicio.setProveedor(this);
        servicios.add(servicio);
    }

    public void darDeBaja() {
        activo = false;
        servicios.forEach(Servicio::darDeBaja);
    }

    public boolean ofrece(Class<? extends Servicio> tipo) {
        return servicios.stream().anyMatch(tipo::isInstance);
    }

    public boolean tieneServiciosDisponibles() {
        return servicios.stream().anyMatch(Servicio::estaDisponible);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNombreLegal() {
        return nombreLegal;
    }

    public void setNombreLegal(String nombreLegal) {
        this.nombreLegal = Validar.texto(nombreLegal, "nombreLegal");
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = Validar.texto(cuit, "cuit");
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = Validar.texto(rubro, "rubro");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Validar.texto(email, "email");
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean esPropio() {
        return propio;
    }

    public boolean estaActivo() {
        return activo;
    }

    public List<Servicio> getServicios() {
        return List.copyOf(servicios);
    }
}
