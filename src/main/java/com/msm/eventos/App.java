package com.msm.eventos;

import com.msm.eventos.aplicacion.AsignadorRecursos;
import com.msm.eventos.aplicacion.CalculadorCostoEvento;
import com.msm.eventos.aplicacion.DesgloseCosto;
import com.msm.eventos.aplicacion.FiltroProveedor;
import com.msm.eventos.aplicacion.GestorPersonal;
import com.msm.eventos.aplicacion.GestorProveedores;
import com.msm.eventos.aplicacion.GestorServicios;
import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.evento.HojaServicio;
import com.msm.eventos.dominio.proveedor.Proveedor;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;
import com.msm.eventos.dominio.recurso.DisponibilidadRecurso;
import com.msm.eventos.dominio.recurso.Locacion;
import com.msm.eventos.dominio.recurso.Personal;
import com.msm.eventos.dominio.recurso.TipoPersonal;
import com.msm.eventos.dominio.servicio.CategoriaEntretenimiento;
import com.msm.eventos.dominio.servicio.Entretenimiento;
import com.msm.eventos.dominio.servicio.Ingrediente;
import com.msm.eventos.dominio.servicio.Menu;
import com.msm.eventos.dominio.servicio.MenuPropio;
import com.msm.eventos.dominio.servicio.Mobiliario;
import com.msm.eventos.dominio.servicio.Mueble;
import com.msm.eventos.dominio.servicio.Plato;
import com.msm.eventos.dominio.servicio.RequerimientoPersonal;
import com.msm.eventos.dominio.servicio.Servicio;
import com.msm.eventos.dominio.servicio.ServicioCompuesto;
import com.msm.eventos.dominio.servicio.TipoDieta;
import com.msm.eventos.dominio.servicio.TipoPlato;
import com.msm.eventos.infraestructura.notificacion.NotificadorConsola;
import com.msm.eventos.infraestructura.persistencia.RepositorioEnMemoria;

import java.time.LocalDateTime;
import java.util.List;

public class App {

    public static void main(String[] args) {
        Repositorio<Proveedor> proveedores = new RepositorioEnMemoria<>();
        Repositorio<Servicio> servicios = new RepositorioEnMemoria<>();
        Repositorio<Personal> personal = new RepositorioEnMemoria<>();
        Repositorio<DisponibilidadRecurso> disponibilidades = new RepositorioEnMemoria<>();
        Repositorio<AsignacionRecurso> asignaciones = new RepositorioEnMemoria<>();

        GestorProveedores gestorProveedores = new GestorProveedores(proveedores, servicios);
        GestorServicios gestorServicios = new GestorServicios(servicios, proveedores);
        GestorPersonal gestorPersonal = new GestorPersonal(personal, disponibilidades);
        AsignadorRecursos asignador = new AsignadorRecursos(disponibilidades, asignaciones, personal, new NotificadorConsola());
        CalculadorCostoEvento calculador = new CalculadorCostoEvento(500);

        Proveedor propio = gestorProveedores.registrar(
                new Proveedor("MSM Eventos", "30-11111111-1", "Catering", "cocina@msm.com", true));
        Proveedor sonido = gestorProveedores.registrar(
                new Proveedor("Sonido Sur SRL", "30-22222222-2", "Sonido", "contacto@sonidosur.com", false));

        MenuPropio asado = new MenuPropio("Asado criollo", "Menú tradicional", 30, 0.35, 1500);
        asado.clasificar(TipoDieta.TRADICIONAL, TipoDieta.CELIACO);
        asado.agregarPlato(new Plato("Empanadas", TipoPlato.ENTRADA).con(new Ingrediente("Masa y relleno", 900)));
        asado.agregarPlato(new Plato("Parrillada", TipoPlato.PRINCIPAL).con(new Ingrediente("Carne", 6000)));
        asado.agregarPlato(new Plato("Flan", TipoPlato.POSTRE).con(new Ingrediente("Huevos y leche", 700)));
        asado.agregarRequerimiento(RequerimientoPersonal.fijo(TipoPersonal.ASADOR, 1));
        asado.agregarRequerimiento(RequerimientoPersonal.cadaInvitados(TipoPersonal.MOZO, 25));
        gestorServicios.registrar(propio.getId(), asado);

        Mobiliario mobiliario = new Mobiliario("Mobiliario básico", "Mesas y sillas", 20000)
                .con(new Mueble("Mesa redonda", 6, 8000))
                .con(new Mueble("Silla", 60, 900));
        gestorServicios.registrar(propio.getId(), mobiliario);

        Entretenimiento dj = new Entretenimiento("DJ", "Música y luces", CategoriaEntretenimiento.DJ, 50000, 25000);
        dj.agregarRequerimiento(RequerimientoPersonal.fijo(TipoPersonal.PLOMO, 1));
        gestorServicios.registrar(sonido.getId(), dj);

        ServicioCompuesto fiesta = gestorServicios.crearCompuesto(propio.getId(), "Paquete Fiesta",
                "Comida, mobiliario y música", 30000, List.of(asado.getId(), mobiliario.getId(), dj.getId()));

        System.out.println("Servicio compuesto:");
        System.out.println(fiesta.getResumen());

        System.out.println("\nProveedores que ofrecen menús: "
                + gestorProveedores.consultar(new FiltroProveedor(null, Menu.class, true)).stream()
                .map(Proveedor::getNombreLegal).toList());

        LocalDateTime dia = LocalDateTime.of(2026, 11, 14, 0, 0);
        FranjaHoraria jornada = new FranjaHoraria(dia.withHour(8), dia.withHour(23).withMinute(59));
        for (TipoPersonal tipo : List.of(TipoPersonal.ASADOR, TipoPersonal.MOZO, TipoPersonal.MOZO, TipoPersonal.PLOMO)) {
            Personal p = gestorPersonal.registrar(new Personal("Nombre", tipo.name(), tipo.name().toLowerCase() + "@mail.com", tipo, false, 4000));
            asignador.registrarDisponibilidad(p, jornada);
        }

        Locacion quinta = new Locacion("Quinta Los Álamos", "Ruta 2 km 40", 120, 40, 45, 0);
        HojaServicio hoja = new HojaServicio("EVT-00001", 50,
                new FranjaHoraria(dia.withHour(20), dia.withHour(23).withMinute(30)), quinta);
        gestorServicios.agregarAHoja(hoja, fiesta.getId());

        System.out.println();
        asignador.asignarPersonalRequerido(hoja);

        DesgloseCosto costo = calculador.calcular(hoja);
        System.out.printf("%nCosto del evento: servicios=%.2f recursos=%.2f logística=%.2f TOTAL=%.2f%n",
                costo.servicios(), costo.recursos(), costo.logistica(), costo.total());
    }
}
