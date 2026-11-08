package modelo.eventos.entidades.estado;

import java.util.Set;

/**
 * Patrón State: cada estado sabe qué operaciones admite y a qué estado pasa.
 * Reemplaza al enum EstadoEvento del diagrama original, evitando llenar
 * el código de "if (estado == ...)".
 *
 * Cada método de transición devuelve el nuevo estado o lanza
 * TransicionInvalidaException si desde este estado no se puede.
 */
public interface EstadoEvento {

    String getNombre();

    Set<TransicionEvento> transicionesPosibles();

    EstadoEvento aceptar();

    EstadoEvento iniciar();

    EstadoEvento finalizar();

    EstadoEvento reprogramar();

    EstadoEvento cancelar();

    /** Lanza una excepción si en este estado no se pueden modificar los datos (CDU 014). */
    void verificarModificable();

    /**
     * Lanza una excepción si en este estado no se puede eliminar el evento (CDU 012).
     *
     * @param fueAceptado si el evento estuvo ACEPTADO alguna vez
     */
    void verificarEliminable(boolean fueAceptado);

    /** Lanza una excepción si el estado ya no admite ningún cambio (CDU 013, alternativa 1.2). */
    void verificarAdmiteCambios();

    /** Indica si es un estado final (FINALIZADO o CANCELADO): ya no admite cambios. */
    boolean esFinal();

    /** Indica si el evento ocupa la locación en su fecha. */
    boolean ocupaLocacion();
}
