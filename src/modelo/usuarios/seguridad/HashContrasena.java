package modelo.usuarios.seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2 + salt aleatorio (incluido en el JDK, sin
 * librerías externas). La contraseña nunca se guarda en texto plano (RNF002, RNF004).
 * Formato guardado: iteraciones:salt:hash (en Base64).
 */
public final class HashContrasena {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65_536;
    private static final int LARGO_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private HashContrasena() {
    }

    public static String generar(String contrasena) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(contrasena.toCharArray(), salt, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return ITERACIONES + ":" + b64.encodeToString(salt) + ":" + b64.encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) {
            return false;
        }
        String[] partes = guardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        int iteraciones = Integer.parseInt(partes[0]);
        byte[] salt = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        byte[] calculado = pbkdf2(contrasena.toCharArray(), salt, iteraciones);
        // Comparación en tiempo constante
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] pbkdf2(char[] contrasena, byte[] salt, int iteraciones) {
        try {
            PBEKeySpec spec = new PBEKeySpec(contrasena, salt, iteraciones, LARGO_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo generar el hash de la contraseña", e);
        }
    }
}
