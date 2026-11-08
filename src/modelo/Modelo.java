package modelo;

import modelo.eventos.entidades.EventoFactory;
import modelo.eventos.repositorio.EventoRepository;
import modelo.eventos.repositorio.EventoRepositoryEnMemoria;
import modelo.eventos.repositorio.LocacionRepositoryEnMemoria;
import modelo.eventos.repositorio.PlanificacionRepositoryEnMemoria;
import modelo.eventos.repositorio.SolicitudRepository;
import modelo.eventos.repositorio.SolicitudRepositoryEnMemoria;
import modelo.eventos.repositorio.TipoEventoRepositoryEnMemoria;
import modelo.eventos.servicio.GestorCatalogos;
import modelo.eventos.servicio.ReglasPlanificacion;
import modelo.eventos.servicio.SistemaEventos;
import modelo.persistencia.BaseDeDatos;
import modelo.persistencia.ContextoPersistencia;
import modelo.persistencia.EventoRepositoryPostgres;
import modelo.persistencia.GestorBaseDatos;
import modelo.persistencia.HojaServicioRepositoryPostgres;
import modelo.persistencia.LocacionRepositoryPostgres;
import modelo.persistencia.PlanificacionRepositoryPostgres;
import modelo.persistencia.SolicitudRepositoryPostgres;
import modelo.persistencia.TipoEventoRepositoryPostgres;
import modelo.persistencia.TokenRepositoryPostgres;
import modelo.persistencia.UsuarioRepositoryPostgres;
import modelo.usuarios.entidades.rol.RolFactory;
import modelo.usuarios.repositorio.UsuarioRepositoryEnMemoria;
import modelo.usuarios.servicio.GestorUsuarios;
import modelo.usuarios.servicio.ServicioCorreo;

/**
 * Arma el sistema completo: repositorios, reglas y fachadas. Es el único
 * lugar que sabe si se trabaja con PostgreSQL o en memoria; el resto del
 * sistema solo conoce las interfaces (patrón Repository).
 */
public final class Modelo {

    private final GestorUsuarios gestor;
    private final GestorCatalogos catalogos;
    private final SistemaEventos sistema;
    private final GestorBaseDatos gestorBaseDatos;
    private final BaseDeDatos baseDeDatos;

    private Modelo(GestorUsuarios gestor, GestorCatalogos catalogos, SistemaEventos sistema,
                        GestorBaseDatos gestorBaseDatos, BaseDeDatos baseDeDatos) {
        this.gestor = gestor;
        this.catalogos = catalogos;
        this.sistema = sistema;
        this.gestorBaseDatos = gestorBaseDatos;
        this.baseDeDatos = baseDeDatos;
    }

    /** Modo demostración: todo en memoria, se pierde al cerrar. */
    public static Modelo enMemoria(ServicioCorreo correo) {
        UsuarioRepositoryEnMemoria usuarios = new UsuarioRepositoryEnMemoria();
        SolicitudRepositoryEnMemoria solicitudes = new SolicitudRepositoryEnMemoria();
        PlanificacionRepositoryEnMemoria planificaciones = new PlanificacionRepositoryEnMemoria();
        EventoRepositoryEnMemoria eventos = new EventoRepositoryEnMemoria();
        LocacionRepositoryEnMemoria locaciones = new LocacionRepositoryEnMemoria();
        TipoEventoRepositoryEnMemoria tipos = new TipoEventoRepositoryEnMemoria();

        GestorUsuarios gestor = new GestorUsuarios(usuarios, new RolFactory(), correo);
        ReglasPlanificacion reglas = new ReglasPlanificacion(solicitudes, planificaciones, eventos, locaciones);
        GestorCatalogos catalogos = new GestorCatalogos(gestor, locaciones, tipos, solicitudes, eventos, reglas);
        SistemaEventos sistema = new SistemaEventos(gestor, usuarios, solicitudes, planificaciones, eventos,
                locaciones, tipos, new EventoFactory(), reglas);
        gestor.setVerificadorDeReferencias(id -> tieneReferencias(id, solicitudes, eventos));
        return new Modelo(gestor, catalogos, sistema, null, null);
    }

    /**
     * Modo PostgreSQL. La base ya tiene que estar inicializada
     * (BaseDeDatos.inicializar), que crea la base y las tablas si faltan.
     */
    public static Modelo conPostgres(BaseDeDatos bd, ServicioCorreo correo) {
        ContextoPersistencia ctx = new ContextoPersistencia(bd);
        UsuarioRepositoryPostgres usuarios = new UsuarioRepositoryPostgres(ctx);
        SolicitudRepositoryPostgres solicitudes = new SolicitudRepositoryPostgres(ctx);
        PlanificacionRepositoryPostgres planificaciones = new PlanificacionRepositoryPostgres(ctx);
        EventoRepositoryPostgres eventos = new EventoRepositoryPostgres(ctx);
        LocacionRepositoryPostgres locaciones = new LocacionRepositoryPostgres(ctx);
        TipoEventoRepositoryPostgres tipos = new TipoEventoRepositoryPostgres(ctx);

        GestorUsuarios gestor = new GestorUsuarios(usuarios, new RolFactory(), correo,
                new TokenRepositoryPostgres(ctx), ctx);
        ReglasPlanificacion reglas = new ReglasPlanificacion(solicitudes, planificaciones, eventos, locaciones);
        GestorCatalogos catalogos = new GestorCatalogos(gestor, locaciones, tipos, solicitudes, eventos, reglas, ctx);
        SistemaEventos sistema = new SistemaEventos(gestor, usuarios, solicitudes, planificaciones, eventos,
                locaciones, tipos, new EventoFactory(), reglas, new HojaServicioRepositoryPostgres(ctx), ctx);
        GestorBaseDatos gestorBD = new GestorBaseDatos(gestor, bd, ctx, reglas, locaciones);
        gestor.setVerificadorDeReferencias(id -> tieneReferencias(id, solicitudes, eventos));
        return new Modelo(gestor, catalogos, sistema, gestorBD, bd);
    }

    /** Un usuario que es cliente de una solicitud u organizador de un evento no se puede eliminar. */
    private static boolean tieneReferencias(int idUsuario, SolicitudRepository solicitudes,
                                            EventoRepository eventos) {
        return solicitudes.listarTodos().stream().anyMatch(s -> s.getCliente().getIdUsuario() == idUsuario)
                || eventos.listarTodos().stream().anyMatch(e -> e.getOrganizador().getIdUsuario() == idUsuario);
    }

    public GestorUsuarios getGestor() { return gestor; }
    public GestorCatalogos getCatalogos() { return catalogos; }
    public SistemaEventos getSistema() { return sistema; }
    /** null en modo memoria. */
    public GestorBaseDatos getGestorBaseDatos() { return gestorBaseDatos; }
    /** null en modo memoria. */
    public BaseDeDatos getBaseDeDatos() { return baseDeDatos; }
    public boolean usaBaseDeDatos() { return baseDeDatos != null; }
}
