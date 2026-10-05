package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

import java.util.ArrayList;
import java.util.List;

public class Plato {
    private final String nombre;
    private final TipoPlato tipo;
    private final List<Ingrediente> ingredientes = new ArrayList<>();

    public Plato(String nombre, TipoPlato tipo) {
        this.nombre = Validar.texto(nombre, "nombre");
        this.tipo = Validar.requerido(tipo, "tipo");
    }

    public Plato con(Ingrediente ingrediente) {
        ingredientes.add(Validar.requerido(ingrediente, "ingrediente"));
        return this;
    }

    public double costoPorPorcion() {
        return ingredientes.stream().mapToDouble(Ingrediente::costoPorPorcion).sum();
    }

    public String getNombre() {
        return nombre;
    }

    public TipoPlato getTipo() {
        return tipo;
    }

    public List<Ingrediente> getIngredientes() {
        return List.copyOf(ingredientes);
    }
}
