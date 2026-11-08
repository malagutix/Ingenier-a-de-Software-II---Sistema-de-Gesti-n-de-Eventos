package modelo.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Datos de conexión a PostgreSQL, leídos del archivo basedatos.properties
 * que está en la carpeta del proyecto.
 */
public final class ConfiguracionBaseDatos {

    private final String servidor;
    private final int puerto;
    private final String base;
    private final String usuario;
    private final String contrasena;
    private final String carpetaRespaldos;

    public ConfiguracionBaseDatos(String servidor, int puerto, String base, String usuario, String contrasena,
                                  String carpetaRespaldos) {
        if (!base.matches("[a-z_][a-z0-9_]{0,62}")) {
            throw new IllegalArgumentException("El nombre de la base solo puede tener minúsculas, números y _");
        }
        this.servidor = servidor;
        this.puerto = puerto;
        this.base = base;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.carpetaRespaldos = carpetaRespaldos;
    }

    /**
     * Lee la configuración. Devuelve null si el archivo no existe o tiene
     * habilitado=false (en ese caso el sistema trabaja en memoria).
     */
    public static ConfiguracionBaseDatos cargar(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            return null;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(archivo)) {
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        if (!"true".equalsIgnoreCase(p.getProperty("habilitado", "true").trim())) {
            return null;
        }
        int puerto;
        try {
            puerto = Integer.parseInt(p.getProperty("puerto", "5432").trim());
        } catch (NumberFormatException e) {
            throw new IOException("El puerto de basedatos.properties no es un número");
        }
        try {
            return new ConfiguracionBaseDatos(
                    p.getProperty("servidor", "localhost").trim(),
                    puerto,
                    p.getProperty("base", "gestion_eventos").trim(),
                    p.getProperty("usuario", "postgres").trim(),
                    p.getProperty("contrasena", ""),
                    carpetaRespaldos(archivo, p.getProperty("carpetaRespaldos", "respaldos").trim()));
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    /** Una carpeta relativa se toma desde la carpeta de basedatos.properties (la del proyecto). */
    private static String carpetaRespaldos(Path archivo, String carpeta) {
        Path ruta = java.nio.file.Paths.get(carpeta);
        Path base = archivo.toAbsolutePath().getParent();
        return ruta.isAbsolute() || base == null ? carpeta : base.resolve(ruta).toString();
    }

    String url(String nombreBase) {
        return "jdbc:postgresql://" + servidor + ":" + puerto + "/" + nombreBase
                + "?ApplicationName=Sistema%20de%20Eventos&connectTimeout=5&socketTimeout=60";
    }

    public String getServidor() { return servidor; }
    public int getPuerto() { return puerto; }
    public String getBase() { return base; }
    public String getUsuario() { return usuario; }
    String getContrasena() { return contrasena; }
    public String getCarpetaRespaldos() { return carpetaRespaldos; }
}
