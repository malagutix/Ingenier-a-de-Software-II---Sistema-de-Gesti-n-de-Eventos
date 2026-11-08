package controlador;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.HojaServicio;
import modelo.eventos.entidades.estado.TransicionEvento;
import modelo.eventos.excepciones.EventoException;
import modelo.eventos.exportacion.Exportador;
import modelo.eventos.exportacion.ExportadorExcel;
import modelo.eventos.exportacion.ExportadorPDF;
import modelo.eventos.exportacion.ExportadorWord;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import vista.VistaEventos;

/**
 * Controlador de eventos: CDU 011 (crear), CDU 012 (eliminar), CDU 013
 * (validar con el patrón State), CDU 014 (modificar) y CDU 015 (hoja de
 * servicio, exportada con el patrón Strategy).
 */
public class ControladorEventos extends Controlador implements Refrescable {

    /** Estrategias de exportación disponibles (patrón Strategy). */
    private static final List<Exportador> EXPORTADORES = Arrays.<Exportador>asList(
            new ExportadorPDF(), new ExportadorExcel(), new ExportadorWord());

    private final VistaEventos vista;
    private final SistemaEventos sistema;
    private final Usuario organizador;
    private final PlanificadorEventos planificador;

    public ControladorEventos(VistaEventos vista, SistemaEventos sistema, Usuario organizador) {
        this.vista = vista;
        this.sistema = sistema;
        this.organizador = organizador;
        this.planificador = new PlanificadorEventos(sistema, organizador);
        vista.setPermisos(organizador.tienePermiso(Permiso.VALIDAR_EVENTO),
                organizador.tienePermiso(Permiso.GENERAR_HOJA_SERVICIO));
        vista.alPlanificar(this::crear);
        vista.alModificar(this::modificar);
        vista.alValidar(this::validar);
        vista.alGenerarHoja(this::hojaDeServicio);
        vista.alEliminar(this::eliminar);
        refrescar();
    }

    @Override
    public void refrescar() {
        refrescar(null);
    }

    private void refrescar(String mensajeExito) {
        List<Evento> eventos = cargar(vista, () -> sistema.listarEventos(organizador));
        if (eventos == null) {
            return;
        }
        vista.mostrarEventos(eventos);
        vista.mostrarEstado(mensajeExito);
    }

    // CDU 011 - Crear evento (uno o varios, dentro de una planificación)
    private void crear() {
        List<Evento> creados = planificador.planificar(vista, null);
        if (!creados.isEmpty()) {
            refrescar(PlanificadorEventos.mensajeExito(creados));
            vista.seleccionar(creados.get(0));
        }
    }

    // CDU 014 - Modificar evento
    private void modificar() {
        Evento e = vista.getSeleccionado();
        if (e == null || !ejecutar(vista, () -> e.getEstado().verificarModificable())) {
            return;
        }
        if (vista.editarEvento(e, sistema.listarTiposActivos(), sistema.listarLocacionesHabilitadas(),
                datos -> ejecutar(vista, () -> sistema.modificarEvento(organizador, e.getIdEvento(), datos)))) {
            refrescar("Evento modificado correctamente");
        }
    }

    // CDU 013 - Validar evento (el patrón State decide qué transiciones son posibles)
    private void validar() {
        Evento e = vista.getSeleccionado();
        if (e == null || !ejecutar(vista, () -> e.getEstado().verificarAdmiteCambios())) {
            return;
        }
        TransicionEvento transicion = vista.elegirTransicion(e, e.getEstado().transicionesPosibles());
        if (transicion == null) {
            return;
        }
        LocalDate nuevaFecha = null;
        if (transicion == TransicionEvento.REPROGRAMAR) {
            nuevaFecha = vista.pedirNuevaFecha(e.getFecha().plusDays(7));
            if (nuevaFecha == null) {
                return;
            }
        } else if (transicion == TransicionEvento.CANCELAR
                && !vista.confirmar("¿Cancelar el evento #" + e.getIdEvento() + "? Esta acción no se puede deshacer.",
                        "Cancelar evento")) {
            return;
        }
        LocalDate fechaElegida = nuevaFecha;
        if (ejecutar(vista, () -> sistema.cambiarEstado(organizador, e.getIdEvento(), transicion, fechaElegida))) {
            refrescar("Evento validado con éxito: ahora está " + e.getEstado().getNombre());
        }
    }

    // CDU 015 - Generar hoja de servicio
    private void hojaDeServicio() {
        Evento e = vista.getSeleccionado();
        if (e == null) {
            return;
        }
        HojaServicio hoja = consultar(vista, () -> sistema.generarHojaServicio(organizador, e.getIdEvento()));
        if (hoja == null) {
            return;
        }
        List<String> formatos = new ArrayList<>();
        for (Exportador exportador : EXPORTADORES) {
            formatos.add(exportador.getNombre());
        }
        int elegido;
        while ((elegido = vista.mostrarHoja(hoja, formatos)) >= 0) {
            exportar(hoja, EXPORTADORES.get(elegido));
        }
    }

    private void exportar(HojaServicio hoja, Exportador exportador) {
        File archivo = vista.elegirArchivo("hoja-servicio-evento-" + hoja.getIdEvento() + "." + exportador.getExtension(),
                exportador.getExtension());
        if (archivo == null) {
            return;
        }
        try {
            Files.write(archivo.toPath(), hoja.exportar(exportador));
        } catch (IOException ex) {
            vista.mostrarError("No se pudo guardar el archivo: " + ex.getMessage());
            return;
        }
        vista.ofrecerAbrir(archivo);
    }

    // CDU 012 - Eliminar evento (uno o varios)
    private void eliminar() {
        List<Evento> seleccionados = vista.getSeleccionados();
        if (seleccionados.isEmpty()) {
            return;
        }
        List<Integer> ids = new ArrayList<>();
        List<String> descripciones = new ArrayList<>();
        for (Evento e : seleccionados) {
            String descripcion = "#" + e.getIdEvento() + " (" + e.getTipo() + ", " + Formato.fecha(e.getFecha()) + ")";
            try {
                e.verificarEliminable();
            } catch (EventoException ex) {
                vista.mostrarError(ex.getMessage() + (seleccionados.size() > 1 ? ": el evento " + descripcion : ""));
                return;
            }
            ids.add(e.getIdEvento());
            descripciones.add(descripcion);
        }
        String cuales = ids.size() == 1 ? "el evento " + descripciones.get(0)
                : "los " + ids.size() + " eventos seleccionados:\n" + String.join("\n", descripciones);
        if (vista.confirmar("¿Eliminar " + cuales + "?\nLa solicitud de origen queda libre para crear otro evento.",
                "Eliminar evento")
                && ejecutar(vista, () -> sistema.eliminarEventos(organizador, ids))) {
            refrescar(ids.size() == 1 ? "Evento eliminado" : ids.size() + " eventos eliminados");
        }
    }
}
