package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/** Se cambió la fecha: hay que volver a aceptarlo antes de iniciarlo. */
public class EstadoReprogramado extends EstadoBase {

    @Override
    public String getNombre() { return "REPROGRAMADO"; }

    @Override
    public Set<TransicionEvento> transicionesPosibles() {
        return EnumSet.of(TransicionEvento.ACEPTAR, TransicionEvento.REPROGRAMAR, TransicionEvento.CANCELAR);
    }

    @Override
    public EstadoEvento aceptar() { return new EstadoAceptado(); }

    @Override
    public EstadoEvento reprogramar() { return new EstadoReprogramado(); }

    @Override
    public EstadoEvento cancelar() { return new EstadoCancelado(); }

    @Override
    public void verificarEliminable(boolean fueAceptado) {
        if (fueAceptado) {
            throw new TransicionInvalidaException(
                    "No se puede eliminar un evento que ya había sido aceptado. Si no se va a realizar, cancelalo");
        }
    }
}
