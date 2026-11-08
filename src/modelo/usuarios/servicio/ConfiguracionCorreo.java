package modelo.usuarios.servicio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuración del servidor de correo, leída del archivo correo.properties
 * (ver correo.properties.ejemplo en la carpeta del proyecto).
 */
public final class ConfiguracionCorreo {

    /** Tipo de conexión segura con el servidor. */
    public enum Seguridad { SSL, STARTTLS, NINGUNA }

    private final String servidor;
    private final int puerto;
    private final Seguridad seguridad;
    private final String usuario;
    private final String contrasena;
    private final String remitente;
    private final String nombreRemitente;

    public ConfiguracionCorreo(String servidor, int puerto, Seguridad seguridad, String usuario, String contrasena,
                               String remitente, String nombreRemitente) {
        this.servidor = servidor;
        this.puerto = puerto;
        this.seguridad = seguridad;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.remitente = remitente;
        this.nombreRemitente = nombreRemitente;
    }

    /**
     * Lee la configuración. Devuelve null si el archivo no existe o si tiene
     * habilitado=false (en ese caso se usa la bandeja simulada).
     */
    public static ConfiguracionCorreo cargar(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            return null;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(archivo)) {
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        if (!"true".equalsIgnoreCase(p.getProperty("habilitado", "false").trim())) {
            return null;
        }
        String servidor = obligatorio(p, "servidor");
        int puerto;
        try {
            puerto = Integer.parseInt(obligatorio(p, "puerto"));
        } catch (NumberFormatException e) {
            throw new IOException("El puerto de correo.properties no es un número");
        }
        Seguridad seguridad;
        try {
            seguridad = Seguridad.valueOf(p.getProperty("seguridad", "SSL").trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IOException("En correo.properties, seguridad debe ser SSL, STARTTLS o NINGUNA");
        }
        String usuario = p.getProperty("usuario", "").trim();
        String remitente = p.getProperty("remitente", usuario).trim();
        if (remitente.isEmpty()) {
            throw new IOException("Falta el remitente en correo.properties");
        }
        return new ConfiguracionCorreo(servidor, puerto, seguridad, usuario,
                p.getProperty("contrasena", ""), remitente,
                p.getProperty("nombreRemitente", "Sistema de Gestión de Eventos").trim());
    }

    private static String obligatorio(Properties p, String clave) throws IOException {
        String valor = p.getProperty(clave, "").trim();
        if (valor.isEmpty()) {
            throw new IOException("Falta '" + clave + "' en correo.properties");
        }
        return valor;
    }

    public String getServidor() { return servidor; }
    public int getPuerto() { return puerto; }
    public Seguridad getSeguridad() { return seguridad; }
    public String getUsuario() { return usuario; }
    public String getContrasena() { return contrasena; }
    public String getRemitente() { return remitente; }
    public String getNombreRemitente() { return nombreRemitente; }
}
