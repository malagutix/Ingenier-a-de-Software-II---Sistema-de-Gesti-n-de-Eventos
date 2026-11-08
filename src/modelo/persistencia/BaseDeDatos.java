package modelo.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Conexión con PostgreSQL. Al iniciar sigue el CDU 007: verifica si la base
 * existe, la crea si no existe y genera las tablas y relaciones.
 */
public class BaseDeDatos {

    /** Resultado de la puesta en marcha, para informar al administrador. */
    public static final class Inicio {
        private final boolean baseCreada;
        private final String versionServidor;

        Inicio(boolean baseCreada, String versionServidor) {
            this.baseCreada = baseCreada;
            this.versionServidor = versionServidor;
        }

        public boolean isBaseCreada() { return baseCreada; }
        public String getVersionServidor() { return versionServidor; }
    }

    private final ConfiguracionBaseDatos config;
    private Connection conexion;
    private Inicio inicio;

    public BaseDeDatos(ConfiguracionBaseDatos config) {
        this.config = config;
    }

    /**
     * CDU 007, pasos 1 a 3. Si la base ya existe, omite la creación (2.1).
     * Si no hay conexión con el servidor, lanza ErrorBaseDatosException (7.1).
     */
    public synchronized Inicio inicializar() {
        boolean creada = false;
        try (Connection admin = abrir("postgres")) {
            try (PreparedStatement ps = admin.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
                ps.setString(1, config.getBase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        try (Statement st = admin.createStatement()) {
                            // el nombre ya se validó en ConfiguracionBaseDatos (solo [a-z0-9_])
                            st.execute("CREATE DATABASE \"" + config.getBase() + "\" ENCODING 'UTF8'");
                        }
                        creada = true;
                    }
                }
            }
        } catch (SQLException e) {
            String estado = e.getSQLState() == null ? "" : e.getSQLState();
            // Si no se puede entrar a la base "postgres" (servidores administrados, permisos), se
            // intenta directamente con la base configurada, que puede haber sido creada a mano.
            // 42P04 o 23505: otra computadora la creó en el mismo momento.
            boolean seguir = estado.equals("3D000") || estado.equals("42501") || estado.equals("42P04")
                    || estado.equals("23505");
            if (!seguir || esFallaDeConexion(e)) {
                throw traducir(e, "verificar la base de datos");
            }
        }
        try {
            Connection c = conexion();
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                st.execute("SELECT pg_advisory_xact_lock(4242)"); // dos instancias iniciando a la vez
                for (String sql : Esquema.SENTENCIAS) {
                    st.execute(sql);
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
            String version;
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SHOW server_version")) {
                rs.next();
                version = rs.getString(1);
            }
            inicio = new Inicio(creada, version);
            return inicio;
        } catch (SQLException e) {
            throw traducir(e, "crear las tablas");
        }
    }

    /** Conexión de trabajo (se reabre si se cayó). */
    synchronized Connection conexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = abrir(config.getBase());
        }
        return conexion;
    }

    private Connection abrir(String nombreBase) throws SQLException {
        Properties p = new Properties();
        p.setProperty("user", config.getUsuario());
        p.setProperty("password", config.getContrasena());
        try {
            return DriverManager.getConnection(config.url(nombreBase), p);
        } catch (SQLException e) {
            if (e.getMessage() == null || !e.getMessage().contains("TimeZone")) {
                throw e;
            }
            // El driver envía la zona horaria de Java (por ejemplo, America/Buenos_Aires) y algunos
            // servidores no reconocen ese nombre. El sistema no usa la zona horaria de la sesión
            // (guarda fechas y horas locales), así que se conecta informando UTC.
            synchronized (BaseDeDatos.class) {
                TimeZone original = TimeZone.getDefault();
                try {
                    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
                    return DriverManager.getConnection(config.url(nombreBase), p);
                } finally {
                    TimeZone.setDefault(original);
                }
            }
        }
    }

    /** Descarta la conexión actual para que la próxima operación reconecte. */
    synchronized void descartarConexion() {
        try {
            if (conexion != null) {
                conexion.close();
            }
        } catch (SQLException ignorada) {
            // ya estaba cerrada
        }
        conexion = null;
    }

    public synchronized void cerrar() {
        descartarConexion();
    }

    /** Convierte un error de JDBC en un mensaje entendible para el usuario. */
    ErrorBaseDatosException traducir(SQLException e, String accion) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String mensaje;
        if (esFallaDeConexion(e)) {
            descartarConexion();
            mensaje = "No se pudo conectar con la base de datos en " + config.getServidor() + ":"
                    + config.getPuerto() + ". Verificá que PostgreSQL esté funcionando.";
        } else if (estado.equals("28P01") || estado.equals("28000")) {
            mensaje = "PostgreSQL rechazó el usuario o la contraseña (usuario " + config.getUsuario() + ").";
        } else if (estado.equals("42501")) {
            mensaje = "El usuario " + config.getUsuario() + " no tiene permisos para " + accion + ".";
        } else if (estado.equals("3D000")) {
            mensaje = "La base de datos " + config.getBase() + " no existe y el usuario " + config.getUsuario()
                    + " no puede crearla. Creala con: CREATE DATABASE " + config.getBase() + ";";
        } else if (estado.equals("23503")) {
            mensaje = "La operación no se puede realizar porque hay datos relacionados.";
        } else if (estado.equals("23505")) {
            mensaje = "Ya existe un registro con esos datos.";
        } else {
            mensaje = "Error de base de datos al " + accion + ": " + e.getMessage();
        }
        return new ErrorBaseDatosException(mensaje, e);
    }

    /** La conexión se cortó (servidor caído, reiniciado o red): conviene reconectar. */
    synchronized boolean esFallaDeConexion(SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        if (estado.startsWith("08") || estado.startsWith("57P")) {
            return true;
        }
        try {
            return conexion != null && conexion.isClosed();
        } catch (SQLException ignorada) {
            return true;
        }
    }

    public ConfiguracionBaseDatos getConfiguracion() { return config; }
    public Inicio getInicio() { return inicio; }
}
