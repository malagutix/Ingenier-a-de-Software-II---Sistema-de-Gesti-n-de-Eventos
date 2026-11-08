package controlador;

import java.util.HashMap;
import java.util.Map;

import modelo.Modelo;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import vista.Vista;
import vista.VistaBaseDatos;
import vista.VistaCalendario;
import vista.VistaEventos;
import vista.VistaInicio;
import vista.VistaLocaciones;
import vista.VistaNotificaciones;
import vista.VistaPlanificaciones;
import vista.VistaSolicitudes;
import vista.VistaTiposEvento;
import vista.VistaUsuarios;

/**
 * Controlador de la pantalla principal. Arma una pestaña (vista + controlador)
 * por cada área a la que el rol tiene acceso, recarga la pestaña que se elige
 * y cierra la sesión.
 */
public class ControladorInicio extends Controlador {

    private final VistaInicio vista;
    private final Modelo modelo;
    private final Usuario usuario;
    private final Map<Vista, Refrescable> controladores = new HashMap<>();
    private final VistaNotificaciones vistaNotificaciones = new VistaNotificaciones();

    /**
     * @param motivoSinBase  por qué se trabaja en memoria, o null si hay base de datos
     * @param alCerrarSesion qué hacer después de cerrar la sesión (volver al login)
     */
    public ControladorInicio(VistaInicio vista, Modelo modelo, Usuario usuario, String motivoSinBase,
                             Runnable alCerrarSesion) {
        this.vista = vista;
        this.modelo = modelo;
        this.usuario = usuario;

        if (usuario.tienePermiso(Permiso.GESTIONAR_USUARIOS)) {
            VistaUsuarios v = new VistaUsuarios();
            agregar("Usuarios", v, new ControladorUsuarios(v, modelo.getGestor(), usuario, vista::actualizarEncabezado));
        }
        if (usuario.tienePermiso(Permiso.GESTIONAR_SOLICITUDES)) {
            VistaSolicitudes v = new VistaSolicitudes();
            agregar("Solicitudes", v, new ControladorSolicitudes(v, modelo.getSistema(), usuario));
        }
        if (usuario.tienePermiso(Permiso.GESTIONAR_EVENTOS)) {
            VistaPlanificaciones vp = new VistaPlanificaciones();
            agregar("Planificaciones", vp, new ControladorPlanificaciones(vp, modelo.getSistema(), usuario));
            VistaEventos ve = new VistaEventos();
            agregar("Eventos", ve, new ControladorEventos(ve, modelo.getSistema(), usuario));
        }
        if (usuario.tienePermiso(Permiso.VER_CALENDARIO)) {
            VistaCalendario v = new VistaCalendario(usuario.tienePermiso(Permiso.GESTIONAR_EVENTOS));
            agregar("Calendario", v, new ControladorCalendario(v, modelo.getSistema(), usuario));
        }
        if (usuario.tienePermiso(Permiso.GESTIONAR_LOCACIONES)) {
            VistaLocaciones v = new VistaLocaciones();
            agregar("Locaciones", v, new ControladorLocaciones(v, modelo.getCatalogos(), usuario));
        }
        if (usuario.tienePermiso(Permiso.GESTIONAR_TIPOS_EVENTO)) {
            VistaTiposEvento v = new VistaTiposEvento();
            agregar("Tipos de evento", v, new ControladorTiposEvento(v, modelo.getCatalogos(), usuario));
        }
        if (usuario.tienePermiso(Permiso.GESTIONAR_BD)) {
            VistaBaseDatos v = new VistaBaseDatos(modelo.usaBaseDeDatos(), motivoSinBase);
            agregar("Base de datos", v, new ControladorBaseDatos(v, modelo.getGestorBaseDatos(), usuario, () -> {
                modelo.getGestor().cerrarSesion(usuario);
                alCerrarSesion.run();
            }));
        }
        agregar("Notificaciones", vistaNotificaciones,
                new ControladorNotificaciones(vistaNotificaciones, modelo.getGestor(), usuario,
                        this::actualizarTituloNotificaciones));

        vista.alCambiarPestana(seleccionada -> {
            Refrescable controlador = controladores.get(seleccionada);
            if (controlador != null) {
                controlador.refrescar();
            }
            actualizarTituloNotificaciones();
        });
        vista.alCerrarSesion(() -> {
            modelo.getGestor().cerrarSesion(usuario);
            alCerrarSesion.run();
        });
        actualizarTituloNotificaciones();
    }

    private void agregar(String titulo, Vista vistaPestana, Refrescable controlador) {
        vista.agregarPestana(titulo, vistaPestana);
        controladores.put(vistaPestana, controlador);
    }

    private void actualizarTituloNotificaciones() {
        Integer noLeidas = cargar(vista, () -> modelo.getGestor().contarNoLeidas(usuario));
        int cantidad = noLeidas == null ? 0 : noLeidas;
        vista.setTituloPestana(vistaNotificaciones,
                cantidad == 0 ? "Notificaciones" : "Notificaciones (" + cantidad + ")");
    }
}
