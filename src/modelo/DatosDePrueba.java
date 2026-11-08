package modelo;

import java.time.LocalDate;
import java.time.LocalTime;

import modelo.eventos.entidades.DatosLocacion;
import modelo.eventos.entidades.DatosSolicitud;
import modelo.eventos.entidades.DatosTipoEvento;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.MenuEvento;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.entidades.TipoLocacion;
import modelo.eventos.entidades.estado.TransicionEvento;
import modelo.eventos.servicio.GestorCatalogos;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.servicio.GestorUsuarios;

/** Carga datos de ejemplo para poder probar la aplicación enseguida. */
public final class DatosDePrueba {

    public static final String RESUMEN =
            "Administrador  admin@eventos.com   Admin2026\n"
          + "Organizador    maria@mail.com      Clave2026\n"
          + "Cliente        juan@mail.com       Clave2026\n"
          + "Cliente        lucia@mail.com      Clave2026\n"
          + "Sin rol        sofia@mail.com      Clave2026  (no puede ingresar)";

    /** Usuarios de ejemplo cuando el administrador lo crea uno mismo (modo base de datos). */
    public static final String RESUMEN_SIN_ADMIN =
            "Organizador    maria@mail.com      Clave2026\n"
          + "Cliente        juan@mail.com       Clave2026\n"
          + "Cliente        lucia@mail.com      Clave2026\n"
          + "Sin rol        sofia@mail.com      Clave2026  (no puede ingresar)";

    private DatosDePrueba() {
    }

    /** Modo en memoria: crea también el administrador admin@eventos.com. */
    public static void cargar(GestorUsuarios gestor, GestorCatalogos catalogos, SistemaEventos sistema) {
        gestor.crearAdministradorInicial(new DatosUsuario(
                "Ana", "Pérez", "30111222", "admin@eventos.com", "Admin2026"));
        Usuario admin = gestor.iniciarSesion("admin@eventos.com", "Admin2026");
        cargarEjemplos(gestor, catalogos, sistema, admin);
        gestor.cerrarSesion(admin);
    }

    /**
     * Carga usuarios, tipos, locaciones, solicitudes y planificaciones de
     * ejemplo usando un administrador que ya inició sesión.
     */
    public static void cargarEjemplos(GestorUsuarios gestor, GestorCatalogos catalogos, SistemaEventos sistema,
                                      Usuario admin) {
        // Usuarios
        Usuario maria = gestor.registrarUsuario(new DatosUsuario(
                "María", "Gómez", "35444555", "maria@mail.com", "Clave2026"));
        Usuario juan = gestor.registrarUsuario(new DatosUsuario(
                "Juan", "López", "38777888", "juan@mail.com", "Clave2026"));
        Usuario lucia = gestor.registrarUsuario(new DatosUsuario(
                "Lucía", "Fernández", "39123456", "lucia@mail.com", "Clave2026"));
        gestor.registrarUsuario(new DatosUsuario(
                "Sofía", "Díaz", "42999000", "sofia@mail.com", "Clave2026"));

        gestor.asignarRol(admin, maria.getIdUsuario(), "ORGANIZADOR");
        gestor.asignarRol(admin, juan.getIdUsuario(), "CLIENTE");
        gestor.asignarRol(admin, lucia.getIdUsuario(), "CLIENTE");

        // Tipos de evento del enunciado
        TipoEvento congreso = catalogos.crearTipo(admin, new DatosTipoEvento("Congreso", 8, LocalTime.of(9, 0),
                "Escenario, proyector, sonido y mesa de acreditaciones"));
        catalogos.crearTipo(admin, new DatosTipoEvento("Feria", 10, LocalTime.of(10, 0),
                "Stands, señalización y seguridad"));
        TipoEvento boda = catalogos.crearTipo(admin, new DatosTipoEvento("Boda", 7, LocalTime.of(20, 0),
                "Ambientación, pista de baile y DJ"));
        TipoEvento celebracion = catalogos.crearTipo(admin, new DatosTipoEvento("Celebración privada", 5,
                LocalTime.of(20, 30), "Ambientación y música"));
        TipoEvento empresarial = catalogos.crearTipo(admin, new DatosTipoEvento("Evento empresarial", 4,
                LocalTime.of(9, 0), "Proyector, sonido y conexión a internet"));

        // Locaciones
        Locacion costaAzul = catalogos.crearLocacion(admin, new DatosLocacion("Salón Costa Azul",
                "Av. Costanera 1200", TipoLocacion.SALON_PROPIO, 200, 3.5, 15, 120, 2, true,
                "Acceso de carga por calle lateral"));
        Locacion aromos = catalogos.crearLocacion(admin, new DatosLocacion("Quinta Los Aromos",
                "Camino Viejo km 4", TipoLocacion.ESPACIO_ALQUILADO, 120, 18.0, 35, 180, 3, true,
                "Grupo electrógeno de respaldo"));
        Locacion atlantico = catalogos.crearLocacion(admin, new DatosLocacion("Centro de Convenciones Atlántico",
                "Bv. del Mar 3400", TipoLocacion.ESPACIO_ALQUILADO, 400, 6.0, 20, 240, 4, false,
                "Escenario fijo y cabina de sonido"));
        Locacion terraza = catalogos.crearLocacion(admin, new DatosLocacion("Espacio Terraza",
                "Calle San Martín 850, piso 12", TipoLocacion.SALON_PROPIO, 60, 1.2, 10, 60, 1, false,
                "Montacargas hasta el piso 12"));
        catalogos.crearLocacion(admin, new DatosLocacion("Plaza del Puerto", "Explanada del Puerto s/n",
                TipoLocacion.ESPACIO_PUBLICO, 800, 9.0, 25, 300, 5, true,
                "Permiso municipal, baños químicos y vallado"));
        // Un bloqueo de ejemplo (mantenimiento)
        LocalDate hoy = LocalDate.now();
        catalogos.bloquearFechas(admin, terraza.getIdLocacion(), hoy.plusDays(30), hoy.plusDays(32),
                "Mantenimiento del montacargas");

        // Solicitudes (las crea el administrador, CDU 008)
        SolicitudEvento s1 = sistema.crearSolicitud(admin, new DatosSolicitud(juan.getIdUsuario(),
                hoy.plusDays(10), boda.getIdTipoEvento(), 150, costaAzul.getIdLocacion(), MenuEvento.ESTANDAR));
        SolicitudEvento s2 = sistema.crearSolicitud(admin, new DatosSolicitud(lucia.getIdUsuario(),
                hoy.plusDays(18), congreso.getIdTipoEvento(), 110, aromos.getIdLocacion(), MenuEvento.LUNCH));
        sistema.crearSolicitud(admin, new DatosSolicitud(juan.getIdUsuario(),
                hoy.plusDays(25), celebracion.getIdTipoEvento(), 50, terraza.getIdLocacion(), MenuEvento.VEGETARIANO));
        sistema.crearSolicitud(admin, new DatosSolicitud(lucia.getIdUsuario(),
                hoy.plusDays(40), empresarial.getIdTipoEvento(), 300, atlantico.getIdLocacion(), MenuEvento.SIN_TACC));
        // Al aceptarlas se crea su primer evento; la #3 y la #4 quedan PENDIENTES
        Evento casamiento = sistema.aceptarSolicitud(admin, s1.getIdSolicitud(), "Casamiento de Juan");
        Evento jornada1 = sistema.aceptarSolicitud(admin, s2.getIdSolicitud(), "Congreso de Innovación 2026");

        // Planificaciones (las completa y valida la organizadora, CDU 011 y 013)
        Usuario org = gestor.iniciarSesion("maria@mail.com", "Clave2026");
        sistema.planificarEventos(org, s2.getIdSolicitud(), s2.getFechaSolicitada().plusDays(1),
                null, null, 2, null); // jornadas 2 y 3 del congreso
        sistema.cambiarEstado(org, casamiento.getIdEvento(), TransicionEvento.ACEPTAR, null);
        sistema.cambiarEstado(org, jornada1.getIdEvento(), TransicionEvento.ACEPTAR, null);
        gestor.cerrarSesion(org);
    }
}
