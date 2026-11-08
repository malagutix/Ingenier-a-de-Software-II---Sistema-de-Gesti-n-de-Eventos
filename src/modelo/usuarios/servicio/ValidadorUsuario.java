package modelo.usuarios.servicio;

import java.util.regex.Pattern;

import modelo.usuarios.excepciones.DatosInvalidosException;

/** Reglas de formato de los datos del usuario (RF001, RF003, RF006). */
public class ValidadorUsuario {

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern DNI = Pattern.compile("^\\d{7,8}$");
    private static final Pattern NOMBRE = Pattern.compile("^[\\p{L} '-]{2,50}$");
    private static final int LARGO_MINIMO_CONTRASENA = 8;

    public void validarNombre(String nombre, String campo) {
        requerido(nombre, campo);
        if (!NOMBRE.matcher(nombre.trim()).matches()) {
            throw new DatosInvalidosException(campo + " inválido");
        }
    }

    public void validarDni(String dni) {
        requerido(dni, "DNI");
        if (!DNI.matcher(dni.trim()).matches()) {
            throw new DatosInvalidosException("El DNI debe tener 7 u 8 dígitos, sin puntos");
        }
    }

    public void validarCorreo(String correo) {
        requerido(correo, "Correo electrónico");
        if (!CORREO.matcher(correo.trim()).matches()) {
            throw new DatosInvalidosException("Correo electrónico inválido");
        }
    }

    /** Criterios mínimos de seguridad: 8 caracteres, al menos una letra y un número. */
    public void validarContrasena(String contrasena) {
        requerido(contrasena, "Contraseña");
        boolean tieneLetra = contrasena.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = contrasena.chars().anyMatch(Character::isDigit);
        if (contrasena.length() < LARGO_MINIMO_CONTRASENA || !tieneLetra || !tieneNumero) {
            throw new DatosInvalidosException(
                    "La contraseña no cumple los requisitos: mínimo 8 caracteres, con letras y números");
        }
    }

    private void requerido(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo " + campo + " es obligatorio");
        }
    }
}
