package controlador;

import java.util.List;

import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.servicio.GestorCatalogos;
import modelo.usuarios.entidades.Usuario;
import vista.VistaTiposEvento;

/** Controlador de la gestión de tipos de evento: alta, modificación, activación y eliminación. */
public class ControladorTiposEvento extends Controlador implements Refrescable {

    private final VistaTiposEvento vista;
    private final GestorCatalogos catalogos;
    private final Usuario admin;

    public ControladorTiposEvento(VistaTiposEvento vista, GestorCatalogos catalogos, Usuario admin) {
        this.vista = vista;
        this.catalogos = catalogos;
        this.admin = admin;
        vista.alNuevo(this::crear);
        vista.alModificar(this::modificar);
        vista.alCambiarActivacion(this::cambiarActivacion);
        vista.alEliminar(this::eliminar);
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<TipoEvento> tipos = cargar(vista, () -> catalogos.listarTipos(admin));
        if (tipos == null) {
            return;
        }
        vista.mostrarTipos(tipos);
        vista.mostrarEstado(mensajeExito);
    }

    private void crear() {
        if (vista.editarTipo("Nuevo tipo de evento", null,
                datos -> ejecutar(vista, () -> catalogos.crearTipo(admin, datos)))) {
            refrescar("Tipo de evento creado");
        }
    }

    private void modificar() {
        TipoEvento t = vista.getSeleccionado();
        if (t == null) {
            return;
        }
        if (vista.editarTipo("Modificar tipo de evento", t,
                datos -> ejecutar(vista, () -> catalogos.modificarTipo(admin, t.getIdTipoEvento(), datos)))) {
            refrescar("Tipo de evento modificado");
        }
    }

    private void cambiarActivacion() {
        TipoEvento t = vista.getSeleccionado();
        if (t == null) {
            return;
        }
        boolean activar = !t.isActivo();
        if (ejecutar(vista, () -> catalogos.cambiarActivacion(admin, t.getIdTipoEvento(), activar))) {
            refrescar(activar ? "Tipo activado" : "Tipo desactivado");
        }
    }

    private void eliminar() {
        TipoEvento t = vista.getSeleccionado();
        if (t == null) {
            return;
        }
        if (vista.confirmar("¿Eliminar el tipo \"" + t.getNombre() + "\"?", "Eliminar tipo de evento")
                && ejecutar(vista, () -> catalogos.eliminarTipo(admin, t.getIdTipoEvento()))) {
            refrescar("Tipo de evento eliminado");
        }
    }
}
