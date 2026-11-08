package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/** Estado final: el evento se canceló y libera la locación. */
public class EstadoCancelado extends EstadoBase {

    @Override
    public String getNombre() { return "CANCELADO"; }

    @Override
    public Set<TransicionEvento> transicionesPosibles() {
        return EnumSet.noneOf(TransicionEvento.class);
    }

    @Override
    public void verificarModificable() {
        throw new TransicionInvalidaException("No se puede modificar un evento cancelado");
    }

    @Override
    public boolean ocupaLocacion() {
        return false;
    }
}
