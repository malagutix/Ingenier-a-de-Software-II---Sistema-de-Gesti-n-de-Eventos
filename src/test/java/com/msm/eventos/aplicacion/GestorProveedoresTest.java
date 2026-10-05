package com.msm.eventos.aplicacion;

import com.msm.eventos.dominio.proveedor.Proveedor;
import com.msm.eventos.dominio.servicio.Menu;
import com.msm.eventos.dominio.servicio.MenuTercero;
import com.msm.eventos.dominio.servicio.Servicio;
import com.msm.eventos.infraestructura.persistencia.RepositorioEnMemoria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestorProveedoresTest {

    private final RepositorioEnMemoria<Proveedor> proveedores = new RepositorioEnMemoria<>();
    private final RepositorioEnMemoria<Servicio> servicios = new RepositorioEnMemoria<>();
    private final GestorProveedores gestorProveedores = new GestorProveedores(proveedores, servicios);
    private final GestorServicios gestorServicios = new GestorServicios(servicios, proveedores);

    @Test
    void laBajaLogicaDesactivaAlProveedorYSusServiciosSinBorrarlos() {
        Proveedor proveedor = gestorProveedores.registrar(new Proveedor("Chef SA", "30-1", "Catering", "chef@mail.com", false));
        MenuTercero menu = gestorServicios.registrar(proveedor.getId(), new MenuTercero("Menú", "", 0, 100));

        gestorProveedores.darDeBaja(proveedor.getId());

        assertFalse(gestorProveedores.obtener(proveedor.getId()).estaActivo());
        assertFalse(gestorServicios.obtener(menu.getId()).estaActivo());
        assertTrue(gestorProveedores.consultar(FiltroProveedor.todos()).isEmpty());
    }

    @Test
    void filtraProveedoresPorTipoDeServicio() {
        Proveedor chef = gestorProveedores.registrar(new Proveedor("Chef SA", "30-1", "Catering", "chef@mail.com", false));
        gestorProveedores.registrar(new Proveedor("Luces SRL", "30-2", "Iluminación", "luces@mail.com", false));
        gestorServicios.registrar(chef.getId(), new MenuTercero("Menú", "", 0, 100));

        var resultado = gestorProveedores.consultar(new FiltroProveedor(null, Menu.class, true));

        assertEquals(1, resultado.size());
        assertEquals(chef.getId(), resultado.get(0).getId());
    }
}
