package com.msm.eventos.dominio.usuario;

import com.msm.eventos.dominio.comun.GeneradorCodigo;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;

public abstract class Usuario implements Identificable {
    private final String id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private boolean activo = true;

    protected Usuario(String nombre, String apellido, String dni, String email) {
        this.id = GeneradorCodigo.siguiente("USR");
        this.nombre = Validar.texto(nombre, "nombre");
        this.apellido = Validar.texto(apellido, "apellido");
        this.dni = Validar.texto(dni, "dni");
        this.email = Validar.texto(email, "email");
    }

    public void darDeBaja() {
        activo = false;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public boolean estaActivo() {
        return activo;
    }
}
