package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/** El evento fue validado y está confirmado. */
public class EstadoAceptado extends EstadoBase {

    @Override
    public String getNombre() { return "ACEPTADO"; }

    @Override
    public Set<TransicionEvento> transicionesPosibles() {
        return EnumSet.of(TransicionEvento.INICIAR, TransicionEvento.REPROGRAMAR, TransicionEvento.CANCELAR);
    }

    @Override
    public EstadoEvento iniciar() { return new EstadoEnProgreso(); }

    @Override
    public EstadoEvento reprogramar() { return new EstadoReprogramado(); }

    @Override
    public EstadoEvento cancelar() { return new EstadoCancelado(); }

    @Override
    public void verificarEliminable(boolean fueAceptado) {
        throw new TransicionInvalidaException("No se puede eliminar un evento ya aceptado. Si no se va a realizar, cancelalo");
    }
}
