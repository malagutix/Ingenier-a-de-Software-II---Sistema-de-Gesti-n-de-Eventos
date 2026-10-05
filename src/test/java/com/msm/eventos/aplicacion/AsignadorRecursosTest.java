package com.msm.eventos.aplicacion;

import com.msm.eventos.aplicacion.puertos.Notificador;
import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.evento.HojaServicio;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;
import com.msm.eventos.dominio.recurso.Personal;
import com.msm.eventos.dominio.recurso.TipoPersonal;
import com.msm.eventos.dominio.servicio.MenuTercero;
import com.msm.eventos.dominio.servicio.RequerimientoPersonal;
import com.msm.eventos.infraestructura.persistencia.RepositorioEnMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsignadorRecursosTest {

    private static final LocalDateTime DIA = LocalDateTime.of(2026, 11, 14, 0, 0);
    private static final FranjaHoraria JORNADA = new FranjaHoraria(DIA.withHour(8), DIA.withHour(23));
    private static final FranjaHoraria NOCHE = new FranjaHoraria(DIA.withHour(20), DIA.withHour(23));

    private RepositorioEnMemoria<Personal> personal;
    private AsignadorRecursos asignador;
    private final List<String> notificaciones = new ArrayList<>();

    @BeforeEach
    void setUp() {
        personal = new RepositorioEnMemoria<>();
        asignador = new AsignadorRecursos(new RepositorioEnMemoria<>(), new RepositorioEnMemoria<>(), personal,
                new Notificador() {
                    public void notificarAsignacion(AsignacionRecurso a) { notificaciones.add("asignacion"); }
                    public void notificarReprogramacion(AsignacionRecurso a, FranjaHoraria f) { notificaciones.add("reprogramacion"); }
                    public void notificarCancelacion(AsignacionRecurso a) { notificaciones.add("cancelacion"); }
                });
    }

    private Personal mozoDisponible() {
        Personal mozo = personal.guardar(new Personal("Juan", "Pérez", "juan@mail.com", TipoPersonal.MOZO, false, 1000));
        asignador.registrarDisponibilidad(mozo, JORNADA);
        return mozo;
    }

    @Test
    void rechazaAsignarSinDisponibilidadCargada() {
        Personal mozo = personal.guardar(new Personal("Ana", "Gómez", "ana@mail.com", TipoPersonal.MOZO, true, 1000));
        HojaServicio hoja = new HojaServicio("EVT-1", 10, NOCHE, null);

        assertThrows(ReglaNegocioException.class, () -> asignador.asignar(mozo, hoja));
    }

    @Test
    void rechazaSolapamientosYNotificaLaAsignacion() {
        Personal mozo = mozoDisponible();
        asignador.asignar(mozo, new HojaServicio("EVT-1", 10, NOCHE, null));
        HojaServicio superpuesta = new HojaServicio("EVT-2", 10,
                new FranjaHoraria(DIA.withHour(22), DIA.withHour(23)), null);

        assertThrows(ReglaNegocioException.class, () -> asignador.asignar(mozo, superpuesta));
        assertEquals(List.of("asignacion"), notificaciones);
    }

    @Test
    void asignaAutomaticamenteElPersonalQueRequierenLosServicios() {
        mozoDisponible();
        mozoDisponible();
        mozoDisponible();
        MenuTercero menu = new MenuTercero("Menú", "", 0, 100);
        menu.agregarRequerimiento(RequerimientoPersonal.cadaInvitados(TipoPersonal.MOZO, 20));
        HojaServicio hoja = new HojaServicio("EVT-1", 50, NOCHE, null);
        hoja.agregarServicio(menu);

        List<AsignacionRecurso> asignadas = asignador.asignarPersonalRequerido(hoja);

        assertEquals(3, asignadas.size());
        assertTrue(asignador.personalFaltante(hoja).isEmpty());
    }

    @Test
    void alReprogramarLaHojaCancelaLoQueNoPuedeMoverse() {
        Personal mozo = mozoDisponible();
        HojaServicio hoja = new HojaServicio("EVT-1", 10, NOCHE, null);
        asignador.asignar(mozo, hoja);

        FranjaHoraria otroDia = new FranjaHoraria(DIA.plusDays(1).withHour(20), DIA.plusDays(1).withHour(23));
        List<AsignacionRecurso> canceladas = asignador.reprogramarHoja(hoja, otroDia);

        assertEquals(1, canceladas.size());
        assertTrue(hoja.getAsignaciones().isEmpty());
        assertEquals(List.of("asignacion", "cancelacion"), notificaciones);
    }
}
