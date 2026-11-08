package modelo.persistencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.EstadoSolicitud;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.MenuEvento;
import modelo.eventos.entidades.Planificacion;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.entidades.TipoLocacion;
import modelo.eventos.entidades.estado.EstadoEvento;
import modelo.eventos.entidades.estado.Estados;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.entidades.rol.RolFactory;

/**
 * Corazón de la persistencia. Cumple dos funciones:
 *
 * 1) Mapa de identidades: cada fila de la base corresponde a un único objeto
 *    en memoria, así las referencias entre objetos (un evento y su locación,
 *    por ejemplo) siguen funcionando igual que antes.
 *
 * 2) Unidad de trabajo: cada operación de las fachadas es una transacción.
 *    Al empezar se bloquea la fila de control, así dos computadoras no pueden
 *    validar y guardar a la vez (por ejemplo, reservar la misma locación).
 *    Si otra computadora cambió algo, primero se recargan los datos.
 *
 * La tabla control tiene un número de versión que aumenta con cada cambio:
 * así se sabe cuándo hay que recargar sin leer todo cada vez.
 */
public class ContextoPersistencia implements UnidadDeTrabajo {

    /** Cada cuánto, como mínimo, se consulta si otra computadora cambió algo. */
    private static final long INTERVALO_VERIFICACION_MS = 1000;

    /** Escritura en la base dentro de la transacción actual. */
    interface Escritura<T> {
        T ejecutar(Connection c) throws SQLException;
    }

    private final BaseDeDatos bd;
    private final RolFactory rolFactory = new RolFactory();

    final Map<Integer, Usuario> usuarios = new TreeMap<>();
    final Map<Integer, TipoEvento> tipos = new TreeMap<>();
    final Map<Integer, Locacion> locaciones = new TreeMap<>();
    final Map<Integer, SolicitudEvento> solicitudes = new TreeMap<>();
    final Map<Integer, Planificacion> planificaciones = new TreeMap<>();
    final Map<Integer, Evento> eventos = new TreeMap<>();

    private long version = -1;
    private long ultimaVerificacion;
    private int profundidad;
    private boolean huboCambios;

    public ContextoPersistencia(BaseDeDatos bd) {
        this.bd = bd;
    }

    // =================================================================
    // Transacciones
    // =================================================================

    @Override
    public synchronized <T> T ejecutar(Supplier<T> operacion) {
        if (profundidad > 0) { // ya hay una transacción en curso: se suma a ella
            profundidad++;
            try {
                return operacion.get();
            } finally {
                profundidad--;
            }
        }
        Connection c = null;
        long actual = 0;
        for (int intento = 1; ; intento++) {
            try {
                c = bd.conexion();
                c.setAutoCommit(false);
                actual = leerVersion(c, true); // bloquea: las demás computadoras esperan
                break;
            } catch (SQLException e) {
                if (intento == 1 && bd.esFallaDeConexion(e)) {
                    bd.descartarConexion(); // se había cortado: se reconecta y se intenta otra vez
                    continue;
                }
                throw bd.traducir(e, "iniciar la operación");
            }
        }
        profundidad = 1;
        huboCambios = false;
        boolean confirmada = false;
        try {
            if (actual != version) {
                cargar(c, actual);
            }
            T resultado = operacion.get();
            guardarNotificacionesPendientes(c);
            long nueva = actual;
            if (huboCambios) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("UPDATE control SET version = version + 1 RETURNING version")) {
                    rs.next();
                    nueva = rs.getLong(1);
                }
            }
            c.commit();
            confirmada = true;
            version = nueva;
            ultimaVerificacion = System.currentTimeMillis();
            return resultado;
        } catch (SQLException e) {
            throw bd.traducir(e, "guardar los cambios");
        } finally {
            profundidad = 0;
            if (!confirmada) {
                try {
                    c.rollback();
                } catch (SQLException ignorada) {
                    bd.descartarConexion();
                }
                if (huboCambios || hayNotificacionesPendientes()) {
                    version = -1; // los objetos pueden haber quedado a medias: se recargan de la base
                }
            }
            try {
                c.setAutoCommit(true);
            } catch (SQLException ignorada) {
                bd.descartarConexion();
            }
        }
    }

    /** Ejecuta una escritura dentro de la transacción actual (o en una nueva). */
    synchronized <T> T escribir(Escritura<T> escritura) {
        return ejecutar(() -> {
            try {
                huboCambios = true;
                return escritura.ejecutar(bd.conexion());
            } catch (SQLException e) {
                throw bd.traducir(e, "guardar los cambios");
            }
        });
    }

    /**
     * Ejecuta algo dentro de una transacción sin marcar cambios de datos
     * (por ejemplo, leer todo para un respaldo sin que nadie escriba en el medio).
     */
    synchronized <T> T enTransaccion(Escritura<T> operacion) {
        return ejecutar(() -> {
            try {
                return operacion.ejecutar(bd.conexion());
            } catch (SQLException e) {
                throw bd.traducir(e, "acceder a los datos");
            }
        });
    }

    /** Ejecuta una lectura directa en la base (para lo que no se mantiene en memoria). */
    synchronized <T> T leer(Escritura<T> lectura) {
        try {
            return lectura.ejecutar(bd.conexion());
        } catch (SQLException e) {
            if (profundidad == 0 && bd.esFallaDeConexion(e)) {
                bd.descartarConexion();
                try {
                    return lectura.ejecutar(bd.conexion());
                } catch (SQLException e2) {
                    throw bd.traducir(e2, "leer los datos");
                }
            }
            throw bd.traducir(e, "leer los datos");
        }
    }

    /** Antes de leer: si otra computadora cambió algo, recarga los datos. */
    synchronized void asegurarActualizado() {
        if (profundidad > 0) {
            return; // dentro de una transacción los datos ya están al día
        }
        long ahora = System.currentTimeMillis();
        if (version >= 0 && ahora - ultimaVerificacion < INTERVALO_VERIFICACION_MS) {
            return;
        }
        leer(c -> {
            long actual = leerVersion(c, false);
            if (actual != version) {
                cargar(c, actual);
            }
            return null;
        });
        ultimaVerificacion = ahora;
    }

    /** Fuerza a releer todo en la próxima consulta (por ejemplo, después de restaurar un respaldo). */
    public synchronized void invalidar() {
        version = -1;
    }

    private static long leerVersion(Connection c, boolean bloquear) throws SQLException {
        String sql = "SELECT version FROM control WHERE id = 1" + (bloquear ? " FOR UPDATE" : "");
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("Falta la fila de control. Reiniciá la aplicación para recrear las tablas.");
            }
            return rs.getLong(1);
        }
    }

    // =================================================================
    // Notificaciones (Observer): se guardan al confirmar la transacción
    // =================================================================

    /** Hay notificaciones nuevas (todavía sin ID) que guardar. */
    private boolean hayNotificacionesPendientes() {
        for (Usuario u : usuarios.values()) {
            for (Notificacion n : u.getNotificaciones()) {
                if (n.getIdNotificacion() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void guardarNotificacionesPendientes(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO notificaciones (id_usuario, mensaje, creada, leida) VALUES (?, ?, ?, ?) RETURNING id")) {
            for (Usuario u : usuarios.values()) {
                for (Notificacion n : u.getNotificaciones()) {
                    if (n.getIdNotificacion() != 0) {
                        continue;
                    }
                    ps.setInt(1, u.getIdUsuario());
                    ps.setString(2, n.getMensaje());
                    ps.setObject(3, n.getCreada());
                    ps.setBoolean(4, n.isLeida());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        n.setIdNotificacion(rs.getInt(1));
                    }
                    huboCambios = true;
                }
            }
        }
    }

    void registrarUsuarioNuevo(Usuario u) {
        usuarios.put(u.getIdUsuario(), u);
    }

    void quitarUsuario(int id) {
        usuarios.remove(id);
    }

    // =================================================================
    // Carga desde la base: actualiza los objetos existentes en su lugar
    // =================================================================

    private void cargar(Connection c, long nuevaVersion) throws SQLException {
        Map<Integer, Usuario> us = cargarUsuarios(c);
        Map<Integer, TipoEvento> ts = cargarTipos(c);
        Map<Integer, Locacion> ls = cargarLocaciones(c);
        Map<Integer, SolicitudEvento> ss = cargarSolicitudes(c, us, ts, ls);
        Map<Integer, Planificacion> ps = cargarPlanificaciones(c, ss);
        Map<Integer, Evento> es = cargarEventos(c, us, ts, ls, ps);

        reemplazar(usuarios, us);
        reemplazar(tipos, ts);
        reemplazar(locaciones, ls);
        reemplazar(solicitudes, ss);
        reemplazar(planificaciones, ps);
        reemplazar(eventos, es);
        version = nuevaVersion;
    }

    private static <T> void reemplazar(Map<Integer, T> destino, Map<Integer, T> origen) {
        destino.clear();
        destino.putAll(origen);
    }

    private Map<Integer, Usuario> cargarUsuarios(Connection c) throws SQLException {
        Map<Integer, Usuario> resultado = new TreeMap<>();
        String sql = "SELECT id, nombre, apellido, dni, correo, contrasena_hash, rol FROM usuarios ORDER BY id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Usuario u = usuarios.get(id);
                if (u == null) {
                    u = new Usuario(rs.getString("nombre"), rs.getString("apellido"), rs.getString("dni"),
                            rs.getString("correo"), rs.getString("contrasena_hash"));
                    u.setIdUsuario(id);
                } else {
                    u.setNombre(rs.getString("nombre"));
                    u.setApellido(rs.getString("apellido"));
                    u.setDni(rs.getString("dni"));
                    u.setCorreoElectronico(rs.getString("correo"));
                    u.setContrasenaHash(rs.getString("contrasena_hash"));
                }
                String rol = rs.getString("rol");
                if (!u.getRol().getNombre().equals(rol)) {
                    u.cambiarRol(rolFactory.crearRol(rol));
                }
                resultado.put(id, u);
            }
        }
        Map<Integer, List<Notificacion>> avisos = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, id_usuario, mensaje, creada, leida FROM notificaciones ORDER BY id")) {
            while (rs.next()) {
                avisos.computeIfAbsent(rs.getInt("id_usuario"), k -> new ArrayList<>()).add(new Notificacion(
                        rs.getInt("id"), rs.getString("mensaje"), rs.getObject("creada", java.time.LocalDateTime.class),
                        rs.getBoolean("leida")));
            }
        }
        for (Usuario u : resultado.values()) {
            u.restaurarNotificaciones(avisos.getOrDefault(u.getIdUsuario(), new ArrayList<Notificacion>()));
        }
        return resultado;
    }

    private Map<Integer, TipoEvento> cargarTipos(Connection c) throws SQLException {
        Map<Integer, TipoEvento> resultado = new TreeMap<>();
        String sql = "SELECT id, nombre, duracion_sugerida, hora_inicio_sugerida, requerimientos_sugeridos, activo"
                + " FROM tipos_evento ORDER BY id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                int duracion = rs.getInt("duracion_sugerida");
                LocalTime hora = rs.getObject("hora_inicio_sugerida", LocalTime.class);
                String req = rs.getString("requerimientos_sugeridos");
                TipoEvento t = tipos.get(id);
                if (t == null) {
                    t = new TipoEvento(nombre, duracion, hora, req);
                    t.setIdTipoEvento(id);
                } else {
                    t.modificar(nombre, duracion, hora, req);
                }
                if (rs.getBoolean("activo")) {
                    t.activar();
                } else {
                    t.desactivar();
                }
                resultado.put(id, t);
            }
        }
        return resultado;
    }

    private Map<Integer, Locacion> cargarLocaciones(Connection c) throws SQLException {
        Map<Integer, Locacion> resultado = new TreeMap<>();
        String sql = "SELECT id, nombre, direccion, tipo, capacidad, distancia_km, tiempo_traslado_min,"
                + " tiempo_montaje_min, vehiculos_requeridos, requiere_camara_frio, otros_requerimientos, habilitada"
                + " FROM locaciones ORDER BY id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String direccion = rs.getString("direccion");
                TipoLocacion tipo = TipoLocacion.valueOf(rs.getString("tipo"));
                int capacidad = rs.getInt("capacidad");
                BigDecimal km = rs.getBigDecimal("distancia_km");
                int traslado = rs.getInt("tiempo_traslado_min");
                int montaje = rs.getInt("tiempo_montaje_min");
                int vehiculos = rs.getInt("vehiculos_requeridos");
                boolean frio = rs.getBoolean("requiere_camara_frio");
                String otros = rs.getString("otros_requerimientos");
                Locacion l = locaciones.get(id);
                if (l == null) {
                    l = new Locacion(nombre, direccion, tipo, capacidad, km.doubleValue(), traslado, montaje,
                            vehiculos, frio, otros);
                    l.setIdLocacion(id);
                } else {
                    l.modificar(nombre, direccion, tipo, capacidad, km.doubleValue(), traslado, montaje,
                            vehiculos, frio, otros);
                }
                if (rs.getBoolean("habilitada")) {
                    l.habilitar();
                } else {
                    l.inhabilitar();
                }
                resultado.put(id, l);
            }
        }
        Map<Integer, List<BloqueoFecha>> bloqueos = new HashMap<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT id_locacion, desde, hasta, motivo FROM bloqueos_locacion ORDER BY id")) {
            while (rs.next()) {
                bloqueos.computeIfAbsent(rs.getInt(1), k -> new ArrayList<>()).add(new BloqueoFecha(
                        rs.getObject(2, LocalDate.class), rs.getObject(3, LocalDate.class), rs.getString(4)));
            }
        }
        for (Locacion l : resultado.values()) {
            l.restaurarBloqueos(bloqueos.getOrDefault(l.getIdLocacion(), new ArrayList<BloqueoFecha>()));
        }
        return resultado;
    }

    private Map<Integer, SolicitudEvento> cargarSolicitudes(Connection c, Map<Integer, Usuario> us,
                                                            Map<Integer, TipoEvento> ts,
                                                            Map<Integer, Locacion> ls) throws SQLException {
        Map<Integer, SolicitudEvento> resultado = new TreeMap<>();
        String sql = "SELECT id, id_cliente, fecha_solicitada, id_tipo_evento, cantidad_invitados, id_locacion,"
                + " menu, estado FROM solicitudes ORDER BY id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Usuario cliente = us.get(rs.getInt("id_cliente"));
                LocalDate fecha = rs.getObject("fecha_solicitada", LocalDate.class);
                TipoEvento tipo = ts.get(rs.getInt("id_tipo_evento"));
                int invitados = rs.getInt("cantidad_invitados");
                Locacion locacion = ls.get(rs.getInt("id_locacion"));
                MenuEvento menu = MenuEvento.valueOf(rs.getString("menu"));
                SolicitudEvento s = solicitudes.get(id);
                if (s == null) {
                    s = new SolicitudEvento(cliente, fecha, tipo, invitados, locacion, menu);
                    s.setIdSolicitud(id);
                } else {
                    s.modificar(cliente, fecha, tipo, invitados, locacion, menu);
                }
                s.restaurarEstado(EstadoSolicitud.valueOf(rs.getString("estado")));
                resultado.put(id, s);
            }
        }
        return resultado;
    }

    private Map<Integer, Planificacion> cargarPlanificaciones(Connection c, Map<Integer, SolicitudEvento> ss)
            throws SQLException {
        Map<Integer, Planificacion> resultado = new TreeMap<>();
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(
                "SELECT id, nombre, id_solicitud FROM planificaciones ORDER BY id")) {
            while (rs.next()) {
                int id = rs.getInt("id");
                SolicitudEvento solicitud = ss.get(rs.getInt("id_solicitud"));
                Planificacion p = planificaciones.get(id);
                if (p == null || p.getSolicitud() != solicitud) {
                    p = new Planificacion(rs.getString("nombre"), solicitud);
                    p.setIdPlanificacion(id);
                } else {
                    p.renombrar(rs.getString("nombre"));
                }
                resultado.put(id, p);
            }
        }
        return resultado;
    }

    private Map<Integer, Evento> cargarEventos(Connection c, Map<Integer, Usuario> us, Map<Integer, TipoEvento> ts,
                                               Map<Integer, Locacion> ls, Map<Integer, Planificacion> ps)
            throws SQLException {
        Map<Integer, Evento> resultado = new TreeMap<>();
        Map<Integer, List<Evento>> porPlanificacion = new HashMap<>();
        String sql = "SELECT id, id_planificacion, fecha, hora_inicio, duracion_horas, cantidad_asistentes,"
                + " requerimientos_especificos, id_tipo_evento, id_locacion, id_organizador, estado,"
                + " fue_aceptado FROM eventos ORDER BY id";
        List<Usuario> todos = new ArrayList<>(us.values());
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Planificacion planificacion = ps.get(rs.getInt("id_planificacion"));
                LocalDate fecha = rs.getObject("fecha", LocalDate.class);
                LocalTime hora = rs.getObject("hora_inicio", LocalTime.class);
                int duracion = rs.getInt("duracion_horas");
                int asistentes = rs.getInt("cantidad_asistentes");
                String req = rs.getString("requerimientos_especificos");
                TipoEvento tipo = ts.get(rs.getInt("id_tipo_evento"));
                Locacion locacion = ls.get(rs.getInt("id_locacion"));
                Usuario organizador = us.get(rs.getInt("id_organizador"));
                EstadoEvento estado = Estados.desdeNombre(rs.getString("estado"));
                Evento e = eventos.get(id);
                if (e == null || e.getPlanificacion() != planificacion || e.getOrganizador() != organizador) {
                    e = new Evento(fecha, hora, duracion, asistentes, req, tipo, locacion, planificacion, organizador,
                            estado);
                    e.setIdEvento(id);
                } else {
                    e.restaurar(fecha, hora, duracion, asistentes, req, tipo, locacion, estado);
                }
                e.restaurarFueAceptado(rs.getBoolean("fue_aceptado"));
                // Observer: las suscripciones no se guardan, se vuelven a armar con la misma regla
                e.quitarTodosLosObservers();
                SistemaEventos.suscribirObservadores(e, organizador, planificacion.getCliente(), todos);
                resultado.put(id, e);
                porPlanificacion.computeIfAbsent(planificacion.getIdPlanificacion(), k -> new ArrayList<>()).add(e);
            }
        }
        for (Planificacion p : ps.values()) {
            p.restaurarEventos(porPlanificacion.getOrDefault(p.getIdPlanificacion(), new ArrayList<Evento>()));
        }
        return resultado;
    }

    BaseDeDatos getBaseDeDatos() {
        return bd;
    }
}
