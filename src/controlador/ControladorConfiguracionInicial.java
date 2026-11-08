package controlador;

import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import modelo.DatosDePrueba;
import modelo.Modelo;
import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Usuario;
import vista.VistaConfiguracionInicial;

/** Controlador de la configuración inicial: crea el primer administrador y, opcionalmente, los datos de ejemplo. */
public class ControladorConfiguracionInicial extends Controlador {

    private final VistaConfiguracionInicial vista;
    private final Modelo modelo;
    private final Consumer<Boolean> alTerminar;

    /** @param alTerminar recibe true si se cargaron los datos de ejemplo */
    public ControladorConfiguracionInicial(VistaConfiguracionInicial vista, Modelo modelo,
                                           Consumer<Boolean> alTerminar) {
        this.vista = vista;
        this.modelo = modelo;
        this.alTerminar = alTerminar;
        vista.alCrear(this::crearAdministrador);
    }

    private void crearAdministrador() {
        DatosUsuario datos = vista.leerDatos();
        if (datos == null) {
            return;
        }
        boolean conEjemplos = vista.isCargarEjemplos();
        vista.mostrarTrabajando(conEjemplos ? "Creando el administrador y los datos de ejemplo..." : "Creando...");
        // se deja que la pantalla muestre el mensaje antes de trabajar
        SwingUtilities.invokeLater(() -> {
            boolean ok = ejecutar(vista, () -> {
                modelo.getGestor().crearAdministradorInicial(datos);
                if (conEjemplos) {
                    Usuario admin = modelo.getGestor().iniciarSesion(datos.correoElectronico(), datos.contrasena());
                    DatosDePrueba.cargarEjemplos(modelo.getGestor(), modelo.getCatalogos(), modelo.getSistema(), admin);
                    modelo.getGestor().cerrarSesion(admin);
                    modelo.getGestorBaseDatos().marcarDatosDeEjemplo();
                }
            });
            vista.terminarTrabajo(!ok);
            if (ok) {
                vista.mostrarInfo("Administrador creado. Ya podés iniciar sesión con "
                        + datos.correoElectronico().trim()
                        + (conEjemplos ? ".\nTambién se cargaron los datos de ejemplo." : "."));
                alTerminar.accept(conEjemplos);
            }
        });
    }
}
