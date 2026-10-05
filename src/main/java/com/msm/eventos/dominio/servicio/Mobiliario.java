package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;

import java.util.ArrayList;
import java.util.List;

public class Mobiliario extends Servicio {
    private final List<Mueble> muebles = new ArrayList<>();

    public Mobiliario(String nombre, String descripcion, double costoBase) {
        super(nombre, descripcion, costoBase);
    }

    @Override
    protected double costoVariable(ParametrosCosto parametros) {
        return muebles.stream().mapToDouble(Mueble::subtotal).sum();
    }

    public Mobiliario con(Mueble mueble) {
        muebles.add(Validar.requerido(mueble, "mueble"));
        return this;
    }

    public List<Mueble> getMuebles() {
        return List.copyOf(muebles);
    }
}
