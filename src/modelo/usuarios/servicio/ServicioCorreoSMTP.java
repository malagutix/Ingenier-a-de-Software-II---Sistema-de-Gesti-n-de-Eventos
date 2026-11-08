package modelo.usuarios.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import modelo.usuarios.excepciones.UsuarioException;

/**
 * Envío real de correos por SMTP (RF003), implementado solo con el JDK.
 * Soporta conexión SSL (puerto 465), STARTTLS (puerto 587) o sin cifrar
 * (solo para pruebas), y autenticación AUTH LOGIN.
 */
public class ServicioCorreoSMTP implements ServicioCorreo {

    private static final int TIEMPO_ESPERA_MS = 15_000;

    private final ConfiguracionCorreo config;
    private Socket socket;
    private BufferedReader entrada;
    private OutputStream salida;

    public ServicioCorreoSMTP(ConfiguracionCorreo config) {
        this.config = config;
    }

    @Override
    public boolean esReal() {
        return true;
    }

    @Override
    public void enviarRecuperacion(String correo, String codigo) {
        String cuerpo = "Hola.\n\n"
                + "Recibimos un pedido para restablecer tu contraseña en el Sistema de Gestión de Eventos.\n\n"
                + "Tu código de recuperación es: " + codigo + "\n\n"
                + "Ingresalo en la aplicación junto con tu nueva contraseña. El código vence en 30 minutos "
                + "y se puede usar una sola vez.\n\n"
                + "Si no pediste este cambio, ignorá este mensaje.\n";
        enviar(correo, "Recuperación de contraseña", cuerpo);
    }

    /** Envía un correo de texto. Lanza UsuarioException si algo falla. */
    public synchronized void enviar(String destinatario, String asunto, String cuerpo) {
        try {
            conectar();
            esperar(220);
            String dominio = "sistema-eventos.local";
            comando("EHLO " + dominio, 250);
            if (config.getSeguridad() == ConfiguracionCorreo.Seguridad.STARTTLS) {
                comando("STARTTLS", 220);
                SSLSocket seguro = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, config.getServidor(), config.getPuerto(), true);
                seguro.startHandshake();
                usar(seguro);
                comando("EHLO " + dominio, 250);
            }
            if (!config.getUsuario().isEmpty()) {
                comando("AUTH LOGIN", 334);
                comando(base64(config.getUsuario()), 334);
                comandoSecreto(base64(config.getContrasena()), 235);
            }
            comando("MAIL FROM:<" + config.getRemitente() + ">", 250);
            comando("RCPT TO:<" + destinatario + ">", 250, 251);
            comando("DATA", 354);
            escribir(mensaje(destinatario, asunto, cuerpo) + "\r\n.");
            esperar(250);
            try {
                comando("QUIT", 221);
            } catch (IOException ignorada) {
                // el correo ya se envió
            }
        } catch (IOException e) {
            throw new UsuarioException("No se pudo enviar el correo: " + e.getMessage()
                    + "\nRevisá la configuración en correo.properties.");
        } finally {
            cerrar();
        }
    }

    private void conectar() throws IOException {
        Socket s;
        if (config.getSeguridad() == ConfiguracionCorreo.Seguridad.SSL) {
            s = SSLSocketFactory.getDefault().createSocket();
        } else {
            s = new Socket();
        }
        s.connect(new InetSocketAddress(config.getServidor(), config.getPuerto()), TIEMPO_ESPERA_MS);
        if (s instanceof SSLSocket) {
            ((SSLSocket) s).startHandshake();
        }
        usar(s);
    }

    private void usar(Socket s) throws IOException {
        socket = s;
        socket.setSoTimeout(TIEMPO_ESPERA_MS);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
        salida = socket.getOutputStream();
    }

    private String mensaje(String destinatario, String asunto, String cuerpo) {
        String fecha = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.ENGLISH));
        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(encabezado(config.getNombreRemitente())).append(" <")
                .append(config.getRemitente()).append(">\r\n");
        sb.append("To: <").append(destinatario).append(">\r\n");
        sb.append("Subject: ").append(encabezado(asunto)).append("\r\n");
        sb.append("Date: ").append(fecha).append("\r\n");
        sb.append("MIME-Version: 1.0\r\n");
        sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
        sb.append("Content-Transfer-Encoding: base64\r\n\r\n");
        String b64 = base64(cuerpo.replace("\n", "\r\n"));
        for (int i = 0; i < b64.length(); i += 76) {
            sb.append(b64, i, Math.min(b64.length(), i + 76)).append("\r\n");
        }
        return sb.toString().replaceAll("\r\n$", "");
    }

    /** Codifica un encabezado con acentos (RFC 2047). */
    private static String encabezado(String texto) {
        return "=?UTF-8?B?" + base64(texto) + "?=";
    }

    private static String base64(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private void comando(String linea, int... esperados) throws IOException {
        escribir(linea);
        esperar(esperados);
    }

    /** Igual que comando() pero sin mostrar el contenido en los mensajes de error. */
    private void comandoSecreto(String linea, int esperado) throws IOException {
        escribir(linea);
        try {
            esperar(esperado);
        } catch (IOException e) {
            throw new IOException("el servidor rechazó el usuario o la contraseña. " + e.getMessage());
        }
    }

    private void escribir(String linea) throws IOException {
        salida.write((linea + "\r\n").getBytes(StandardCharsets.US_ASCII));
        salida.flush();
    }

    /** Lee la respuesta (puede tener varias líneas) y verifica el código. */
    private void esperar(int... esperados) throws IOException {
        String linea;
        StringBuilder respuesta = new StringBuilder();
        do {
            linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("el servidor cerró la conexión");
            }
            respuesta.append(linea).append(' ');
        } while (linea.length() > 3 && linea.charAt(3) == '-');
        int codigo;
        try {
            codigo = Integer.parseInt(linea.substring(0, 3));
        } catch (RuntimeException e) {
            throw new IOException("respuesta inválida del servidor: " + linea);
        }
        for (int esperado : esperados) {
            if (codigo == esperado) {
                return;
            }
        }
        throw new IOException("respuesta del servidor: " + respuesta.toString().trim());
    }

    private void cerrar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignorada) {
            // nada que hacer
        }
        socket = null;
    }
}
