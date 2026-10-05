package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.comun.Validar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServicioCompuesto extends Servicio {
    private final List<Servicio> componentes = new ArrayList<>();

    public ServicioCompuesto(String nombre, String descripcion, double costoBase) {
        super(nombre, descripcion, costoBase);
    }

    public void agregar(Servicio servicio) {
        Validar.requerido(servicio, "servicio");
        if (servicio == this || servicio.contiene(this)) {
            throw new ReglaNegocioException("Un servicio compuesto no puede contenerse a sí mismo");
        }
        if (componentes.contains(servicio)) {
            throw new ReglaNegocioException("El servicio ya forma parte de " + getNombre());
        }
        componentes.add(servicio);
    }

    public void quitar(Servicio servicio) {
        componentes.remove(servicio);
    }

    @Override
    protected double costoVariable(ParametrosCosto parametros) {
        return componentes.stream().mapToDouble(s -> s.calcularCosto(parametros)).sum();
    }

    @Override
    public boolean estaDisponible() {
        return super.estaDisponible()
                && !componentes.isEmpty()
                && componentes.stream().allMatch(Servicio::estaDisponible);
    }

    @Override
    public int getMinimoPersonas() {
        return componentes.stream().mapToInt(Servicio::getMinimoPersonas).max().orElse(0);
    }

    @Override
    public List<RequerimientoPersonal> getRequerimientosPersonal() {
        return Stream.concat(
                super.getRequerimientosPersonal().stream(),
                componentes.stream().flatMap(s -> s.getRequerimientosPersonal().stream())
        ).toList();
    }

    @Override
    public boolean esCompuesto() {
        return true;
    }

    @Override
    public List<Servicio> getComponentes() {
        return List.copyOf(componentes);
    }

    @Override
    public boolean contiene(Servicio otro) {
        return componentes.stream().anyMatch(s -> s == otro || s.contiene(otro));
    }

    @Override
    protected String resumen(String sangria) {
        String hijos = componentes.stream()
                .map(s -> s.resumen(sangria + "   "))
                .collect(Collectors.joining(System.lineSeparator()));
        return super.resumen(sangria) + (hijos.isEmpty() ? "" : System.lineSeparator() + hijos);
    }
}
