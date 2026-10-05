package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.recurso.TipoPersonal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioCompuestoTest {

    private final ParametrosCosto parametros = new ParametrosCosto(10, 2);

    @Test
    void elCostoDelCompuestoSumaRecursivamenteASusComponentes() {
        MenuTercero menu = new MenuTercero("Menú", "", 0, 100);
        Entretenimiento dj = new Entretenimiento("DJ", "", CategoriaEntretenimiento.DJ, 50, 20);
        ServicioCompuesto interno = new ServicioCompuesto("Interno", "", 10);
        interno.agregar(menu);
        ServicioCompuesto externo = new ServicioCompuesto("Externo", "", 5);
        externo.agregar(interno);
        externo.agregar(dj);

        assertEquals(1000, menu.calcularCosto(parametros));
        assertEquals(90, dj.calcularCosto(parametros));
        assertEquals(5 + (10 + 1000) + 90, externo.calcularCosto(parametros));
    }

    @Test
    void noPermiteCiclos() {
        ServicioCompuesto a = new ServicioCompuesto("A", "", 0);
        ServicioCompuesto b = new ServicioCompuesto("B", "", 0);
        a.agregar(b);

        assertThrows(ReglaNegocioException.class, () -> b.agregar(a));
        assertThrows(ReglaNegocioException.class, () -> a.agregar(a));
    }

    @Test
    void noEstaDisponibleSiAlgunComponenteFueDadoDeBaja() {
        MenuTercero menu = new MenuTercero("Menú", "", 0, 100);
        ServicioCompuesto paquete = new ServicioCompuesto("Paquete", "", 0);
        paquete.agregar(menu);
        assertTrue(paquete.estaDisponible());

        menu.darDeBaja();

        assertFalse(paquete.estaDisponible());
    }

    @Test
    void agregaRequerimientosYMinimoDePersonasDeSusComponentes() {
        MenuTercero menu = new MenuTercero("Menú", "", 30, 100);
        menu.agregarRequerimiento(RequerimientoPersonal.cadaInvitados(TipoPersonal.MOZO, 20));
        Entretenimiento banda = new Entretenimiento("Banda", "", CategoriaEntretenimiento.BANDA, 0, 0);
        banda.agregarRequerimiento(RequerimientoPersonal.fijo(TipoPersonal.PLOMO, 2));
        ServicioCompuesto paquete = new ServicioCompuesto("Paquete", "", 0);
        paquete.agregar(menu);
        paquete.agregar(banda);

        assertEquals(2, paquete.getRequerimientosPersonal().size());
        assertEquals(30, paquete.getMinimoPersonas());
    }

    @Test
    void elMenuPropioCalculaSuCostoPorIngredientesYMargen() {
        MenuPropio menu = new MenuPropio("Propio", "", 0, 0.5, 10);
        menu.agregarPlato(new Plato("Principal", TipoPlato.PRINCIPAL).con(new Ingrediente("Carne", 100)));

        assertEquals(160, menu.costoPorPersona());
    }
}
