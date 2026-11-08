package modelo.usuarios.entidades;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Código de recuperación de contraseña (RF003). Se envía por correo, es de un
 * solo uso y vence a los 30 minutos. Formato: XXXX-XXXX (sin caracteres
 * ambiguos como 0/O o 1/I, para que sea fácil de copiar).
 */
public class TokenRecuperacion {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int MINUTOS_VALIDEZ = 30;

    private final String codigo;
    private final Usuario usuario;
    private final LocalDateTime fechaExpiracion;
    private boolean usado;

    public TokenRecuperacion(Usuario usuario) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            if (i == 4) {
                sb.append('-');
            }
            sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        }
        this.codigo = sb.toString();
        this.usuario = usuario;
        this.fechaExpiracion = LocalDateTime.now().plusMinutes(MINUTOS_VALIDEZ);
        this.usado = false;
    }

    /** Reconstruye un código leído de la base de datos. */
    public TokenRecuperacion(String codigo, Usuario usuario, LocalDateTime fechaExpiracion, boolean usado) {
        this.codigo = codigo;
        this.usuario = usuario;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    /** Normaliza lo que escribe el usuario: mayúsculas, sin espacios y con guion. */
    public static String normalizar(String codigo) {
        if (codigo == null) {
            return "";
        }
        String limpio = codigo.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return limpio.length() == 8 ? limpio.substring(0, 4) + "-" + limpio.substring(4) : limpio;
    }

    public boolean esValido() {
        return !usado && LocalDateTime.now().isBefore(fechaExpiracion);
    }

    public void marcarUsado() {
        this.usado = true;
    }

    public String getCodigo() { return codigo; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public boolean isUsado() { return usado; }
}
