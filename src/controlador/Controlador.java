package controlador;

import java.util.function.Supplier;

import modelo.eventos.excepciones.EventoException;
import modelo.persistencia.ErrorBaseDatosException;
import modelo.usuarios.excepciones.UsuarioException;
import vista.Vista;

/**
 * Base de los controladores (la C de MVC). Un controlador recibe las acciones
 * de su vista, le pide al modelo que las realice y le dice a la vista qué
 * mostrar. Cuando el modelo rechaza una operación (por una regla del negocio
 * o porque no hay conexión con la base), el controlador le muestra el motivo
 * al usuario.
 */
public abstract class Controlador {

    /**
     * Ejecuta una operación del modelo.
     *
     * @return true si se realizó; false si el modelo la rechazó (el motivo ya se mostró)
     */
    protected boolean ejecutar(Vista vista, Runnable operacion) {
        return consultar(vista, () -> {
            operacion.run();
            return Boolean.TRUE;
        }) != null;
    }

    /**
     * Pide un dato al modelo.
     *
     * @return el resultado, o null si el modelo rechazó el pedido (el motivo ya se mostró)
     */
    protected <T> T consultar(Vista vista, Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (UsuarioException | EventoException e) {
            vista.mostrarError(e.getMessage());
        } catch (ErrorBaseDatosException e) {
            // CDU 007, alternativa 7.1: sin conexión no se continúa con la operación
            vista.mostrarError("Base de datos", e.getMessage() + "\n\nNo se guardó ningún cambio de esta operación.");
        }
        return null;
    }

    /**
     * Lee datos del modelo para mostrarlos (al abrir o refrescar una pantalla).
     * Si falla, solo se informa el motivo: no hubo ningún cambio que deshacer.
     *
     * @return el resultado, o null si no se pudo leer
     */
    protected <T> T cargar(Vista vista, Supplier<T> lectura) {
        try {
            return lectura.get();
        } catch (UsuarioException | EventoException | ErrorBaseDatosException e) {
            vista.mostrarError(e.getMessage());
            return null;
        }
    }
}
