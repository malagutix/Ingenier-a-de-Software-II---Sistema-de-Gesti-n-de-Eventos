package controlador;

import modelo.DatosDePrueba;
import modelo.Modelo;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.servicio.BandejaCorreoSimulada;
import vista.VistaConfiguracionInicial;
import vista.VistaInicio;
import vista.VistaLogin;
import vista.VistaPrincipal;

/**
 * Controlador de la navegación entre pantallas: configuración inicial (si la
 * base está vacía) → login → inicio → login (al cerrar sesión).
 */
public class ControladorPrincipal extends Controlador {

    private final VistaPrincipal ventana;
    private final Modelo modelo;
    private final BandejaCorreoSimulada bandeja;
    private final String aviso;
    private final String motivoSinBase;
    private VistaLogin vistaLogin;

    /**
     * @param bandeja          bandeja de correo simulada, o null si el correo es real
     * @param usuariosDePrueba texto con los usuarios de ejemplo, o null para no mostrarlo
     * @param aviso            dónde se guardan los datos, se muestra en el login
     * @param motivoSinBase    por qué se trabaja en memoria, o null si hay base de datos
     */
    public ControladorPrincipal(VistaPrincipal ventana, Modelo modelo, BandejaCorreoSimulada bandeja,
                                String usuariosDePrueba, String aviso, String motivoSinBase) {
        this.ventana = ventana;
        this.modelo = modelo;
        this.bandeja = bandeja;
        this.aviso = aviso;
        this.motivoSinBase = motivoSinBase;
        if (modelo.getGestor().sinUsuarios()) {
            VistaConfiguracionInicial configuracion = new VistaConfiguracionInicial();
            new ControladorConfiguracionInicial(configuracion, modelo,
                    conEjemplos -> crearLogin(conEjemplos ? DatosDePrueba.RESUMEN_SIN_ADMIN : null));
            ventana.mostrarConfiguracion(configuracion);
        } else {
            crearLogin(usuariosDePrueba);
        }
    }

    private void crearLogin(String usuariosDePrueba) {
        vistaLogin = new VistaLogin(bandeja != null, usuariosDePrueba, aviso);
        new ControladorLogin(vistaLogin, modelo.getGestor(), bandeja, this::mostrarInicio);
        volverAlLogin();
    }

    private void mostrarInicio(Usuario usuario) {
        VistaInicio inicio = new VistaInicio(usuario);
        new ControladorInicio(inicio, modelo, usuario, motivoSinBase, this::volverAlLogin);
        ventana.mostrarInicio(inicio);
    }

    private void volverAlLogin() {
        vistaLogin.limpiar();
        ventana.mostrarLogin(vistaLogin);
    }
}
