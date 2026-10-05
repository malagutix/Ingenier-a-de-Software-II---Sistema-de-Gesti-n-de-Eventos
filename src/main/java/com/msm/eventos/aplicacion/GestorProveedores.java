package com.msm.eventos.aplicacion;

import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.NoEncontradoException;
import com.msm.eventos.dominio.proveedor.Proveedor;
import com.msm.eventos.dominio.servicio.Servicio;

import java.util.List;
import java.util.function.Consumer;

public class GestorProveedores {
    private final Repositorio<Proveedor> proveedores;
    private final Repositorio<Servicio> servicios;

    public GestorProveedores(Repositorio<Proveedor> proveedores, Repositorio<Servicio> servicios) {
        this.proveedores = proveedores;
        this.servicios = servicios;
    }

    public Proveedor registrar(Proveedor proveedor) {
        return proveedores.guardar(proveedor);
    }

    public Proveedor editar(String id, Consumer<Proveedor> cambios) {
        Proveedor proveedor = obtener(id);
        cambios.accept(proveedor);
        return proveedores.guardar(proveedor);
    }

    public void darDeBaja(String id) {
        Proveedor proveedor = obtener(id);
        proveedor.darDeBaja();
        proveedor.getServicios().forEach(servicios::guardar);
        proveedores.guardar(proveedor);
    }

    public Proveedor obtener(String id) {
        return proveedores.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Proveedor", id));
    }

    public List<Proveedor> consultar(FiltroProveedor filtro) {
        return proveedores.listar(filtro::cumple);
    }
}
