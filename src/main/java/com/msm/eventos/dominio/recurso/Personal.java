package com.msm.eventos.dominio.recurso;

import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Validar;

public class Personal implements RecursoAsignable {
    private final String codigo;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private TipoPersonal tipoPersonal;
    private boolean externo;
    private double tarifaPorHora;
    private boolean activo = true;

    public Personal(String nombre, String apellido, String email, TipoPersonal tipoPersonal,
                    boolean externo, double tarifaPorHora) {
        this.codigo = GeneradorCodigo.siguiente("PER");
        setNombre(nombre);
        setApellido(apellido);
        setEmail(email);
        setTipoPersonal(tipoPersonal);
        this.externo = externo;
        setTarifaPorHora(tarifaPorHora);
    }

    public void darDeBaja() {
        activo = false;
    }

    @Override
    public String getId() {
        return codigo;
    }

    @Override
    public String getDescripcion() {
        return String.format("%s %s (%s)", nombre, apellido, tipoPersonal);
    }

    @Override
    public double getCostoPorHora() {
        return tarifaPorHora;
    }

    @Override
    public boolean estaActivo() {
        return activo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Validar.texto(nombre, "nombre");
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = Validar.texto(apellido, "apellido");
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

    public TipoPersonal getTipoPersonal() {
        return tipoPersonal;
    }

    public void setTipoPersonal(TipoPersonal tipoPersonal) {
        this.tipoPersonal = Validar.requerido(tipoPersonal, "tipoPersonal");
    }

    public boolean esExterno() {
        return externo;
    }

    public void setExterno(boolean externo) {
        this.externo = externo;
    }

    public void setTarifaPorHora(double tarifaPorHora) {
        this.tarifaPorHora = Validar.noNegativo(tarifaPorHora, "tarifaPorHora");
    }
}
