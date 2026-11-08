package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/** El evento se está realizando: solo se puede finalizar. */
public class EstadoEnProgreso extends EstadoBase {

    @Override
    public String getNombre() { return "EN PROGRESO"; }

    @Override
    public Set<TransicionEvento> transicionesPosibles() {
        return EnumSet.of(TransicionEvento.FINALIZAR);
    }

    @Override
    public EstadoEvento finalizar() { return new EstadoFinalizado(); }

    @Override
    public void verificarModificable() {
        throw new TransicionInvalidaException("No se puede modificar un evento en progreso");
    }

    @Override
    public void verificarEliminable(boolean fueAceptado) {
        throw new TransicionInvalidaException("No se puede eliminar un evento en progreso");
    }
}
