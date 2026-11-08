package controlador;

import java.util.List;
import java.util.function.Consumer;

import modelo.usuarios.entidades.CorreoSimulado;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.servicio.BandejaCorreoSimulada;
import modelo.usuarios.servicio.GestorUsuarios;
import vista.VistaLogin.PedidoRecuperacion;
import vista.VistaLogin;

/**
 * Controlador del login: CDU 001 (crear cuenta), CDU 002 (iniciar sesión)
 * y CDU 003 (restablecer contraseña).
 */
public class ControladorLogin extends Controlador {

    private final VistaLogin vista;
    private final GestorUsuarios gestor;
    private final BandejaCorreoSimulada bandeja;
    private final Consumer<Usuario> alIngresar;

    /**
     * @param bandeja    bandeja simulada, o null si el correo es real
     * @param alIngresar qué hacer con el usuario que inició sesión
     */
    public ControladorLogin(VistaLogin vista, GestorUsuarios gestor, BandejaCorreoSimulada bandeja,
                            Consumer<Usuario> alIngresar) {
        this.vista = vista;
        this.gestor = gestor;
        this.bandeja = bandeja;
        this.alIngresar = alIngresar;
        vista.alIngresar(this::ingresar);
        vista.alCrearCuenta(this::crearCuenta);
        vista.alOlvidarContrasena(this::recuperarContrasena);
        vista.alAbrirBandeja(this::abrirBandeja);
    }

    // CDU 002 - Iniciar sesión
    private void ingresar() {
        Usuario usuario = cargar(vista, () -> gestor.iniciarSesion(vista.getCorreo(), vista.getContrasena()));
        if (usuario == null) {
            vista.seleccionarContrasena();
            return;
        }
        vista.limpiarContrasena();
        alIngresar.accept(usuario);
    }

    // CDU 001 - Registro de usuario
    private void crearCuenta() {
        Usuario[] registrado = new Usuario[1];
        boolean ok = vista.pedirNuevoUsuario(datos -> ejecutar(vista,
                () -> registrado[0] = gestor.registrarUsuario(datos)));
        if (ok) {
            vista.setCorreo(registrado[0].getCorreoElectronico());
            vista.mostrarInfo("Usuario registrado correctamente.\n\n"
                    + "Tu cuenta queda con rol PENDIENTE: un administrador tiene que\n"
                    + "asignarte un rol antes de que puedas ingresar.");
        }
    }

    // CDU 003 - Restablecer contraseña
    private void recuperarContrasena() {
        PedidoRecuperacion pedido = vista.pedirCorreoRecuperacion();
        switch (pedido.accion()) {
            case YA_TENGO_CODIGO:
                pedirNuevaContrasena("", null);
                return;
            case CANCELAR:
                return;
            default:
                break;
        }
        String direccion = pedido.correo();
        if (!ejecutar(vista, () -> gestor.solicitarRecuperacion(direccion))) {
            return;
        }
        if (gestor.correoEsReal()) {
            vista.avisarCodigoEnviado(direccion);
            pedirNuevaContrasena("", direccion);
        } else if (vista.preguntarVerCorreo(direccion)) {
            abrirCorreo(bandeja.getUltimo());
        }
    }

    private void abrirBandeja() {
        List<CorreoSimulado> correos = bandeja.getCorreos();
        if (correos.isEmpty()) {
            vista.mostrarInfo("La bandeja está vacía.\nUsá \"Olvidé mi contraseña\" para recibir un correo.");
            return;
        }
        CorreoSimulado elegido = vista.elegirCorreo(correos);
        if (elegido != null) {
            abrirCorreo(elegido);
        }
    }

    private void abrirCorreo(CorreoSimulado mail) {
        if (vista.mostrarCorreo(mail)) {
            pedirNuevaContrasena(mail.codigo(), mail.destinatario());
        }
    }

    private void pedirNuevaContrasena(String codigoInicial, String cuenta) {
        vista.pedirNuevaContrasena(codigoInicial, cuenta, (codigo, nueva, confirmacion) -> {
            String actualizada = consultar(vista, () -> gestor.restablecerContrasena(codigo, nueva, confirmacion));
            if (actualizada == null) {
                return false; // se puede corregir el código o las contraseñas y reintentar
            }
            vista.mostrarInfo("Contraseña de " + actualizada + " actualizada correctamente.\nYa podés iniciar sesión.");
            vista.setCorreo(actualizada);
            return true;
        });
    }
}
