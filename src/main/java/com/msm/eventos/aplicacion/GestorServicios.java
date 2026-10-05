package com.msm.eventos.aplicacion;

import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.NoEncontradoException;
import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.evento.HojaServicio;
import com.msm.eventos.dominio.proveedor.Proveedor;
import com.msm.eventos.dominio.servicio.Menu;
import com.msm.eventos.dominio.servicio.Servicio;
import com.msm.eventos.dominio.servicio.ServicioCompuesto;
import com.msm.eventos.dominio.servicio.TipoDieta;

import java.util.List;
import java.util.function.Consumer;

public class GestorServicios {
    private final Repositorio<Servicio> servicios;
    private final Repositorio<Proveedor> proveedores;

    public GestorServicios(Repositorio<Servicio> servicios, Repositorio<Proveedor> proveedores) {
        this.servicios = servicios;
        this.proveedores = proveedores;
    }

    public <S extends Servicio> S registrar(String idProveedor, S servicio) {
        Proveedor proveedor = proveedores.buscarPorId(idProveedor)
                .orElseThrow(() -> new NoEncontradoException("Proveedor", idProveedor));
        proveedor.agregarServicio(servicio);
        servicios.guardar(servicio);
        proveedores.guardar(proveedor);
        return servicio;
    }

    public ServicioCompuesto crearCompuesto(String idProveedor, String nombre, String descripcion,
                                            double costoBase, List<String> idsComponentes) {
        if (idsComponentes == null || idsComponentes.isEmpty()) {
            throw new ReglaNegocioException("Un servicio compuesto necesita al menos un componente");
        }
        ServicioCompuesto compuesto = new ServicioCompuesto(nombre, descripcion, costoBase);
        idsComponentes.stream().map(this::obtenerDisponible).forEach(compuesto::agregar);
        return registrar(idProveedor, compuesto);
    }

    public ServicioCompuesto agregarComponente(String idCompuesto, String idComponente) {
        ServicioCompuesto compuesto = obtenerCompuesto(idCompuesto);
        compuesto.agregar(obtenerDisponible(idComponente));
        servicios.guardar(compuesto);
        return compuesto;
    }

    public ServicioCompuesto quitarComponente(String idCompuesto, String idComponente) {
        ServicioCompuesto compuesto = obtenerCompuesto(idCompuesto);
        compuesto.quitar(obtener(idComponente));
        servicios.guardar(compuesto);
        return compuesto;
    }

    public Servicio editar(String id, Consumer<Servicio> cambios) {
        Servicio servicio = obtener(id);
        cambios.accept(servicio);
        return servicios.guardar(servicio);
    }

    public void darDeBaja(String id) {
        Servicio servicio = obtener(id);
        servicio.darDeBaja();
        servicios.guardar(servicio);
    }

    public Servicio obtener(String id) {
        return servicios.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Servicio", id));
    }

    public List<Servicio> listarPorProveedor(String idProveedor) {
        return servicios.listar(s -> s.getProveedor() != null && s.getProveedor().getId().equals(idProveedor));
    }

    public List<Menu> listarMenus(TipoDieta dieta) {
        return servicios.listar(s -> s instanceof Menu m && s.estaDisponible() && (dieta == null || m.esApto(dieta)))
                .stream().map(Menu.class::cast).toList();
    }

    public void validarParaEvento(Servicio servicio, int invitados) {
        if (!servicio.estaDisponible()) {
            throw new ReglaNegocioException("El servicio " + servicio.getNombre() + " no está disponible");
        }
        if (invitados < servicio.getMinimoPersonas()) {
            throw new ReglaNegocioException(String.format("%s requiere al menos %d personas",
                    servicio.getNombre(), servicio.getMinimoPersonas()));
        }
    }

    public void agregarAHoja(HojaServicio hoja, String idServicio) {
        Servicio servicio = obtener(idServicio);
        validarParaEvento(servicio, hoja.getCantidadInvitados());
        hoja.agregarServicio(servicio);
    }

    private Servicio obtenerDisponible(String id) {
        Servicio servicio = obtener(id);
        if (!servicio.estaDisponible()) {
            throw new ReglaNegocioException("El servicio " + servicio.getNombre() + " no está disponible");
        }
        return servicio;
    }

    private ServicioCompuesto obtenerCompuesto(String id) {
        if (obtener(id) instanceof ServicioCompuesto compuesto) {
            return compuesto;
        }
        throw new ReglaNegocioException("El servicio " + id + " no es compuesto");
    }
}
