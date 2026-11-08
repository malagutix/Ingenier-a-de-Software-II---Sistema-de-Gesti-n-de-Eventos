package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/** Estado final: el evento ya se realizó. */
public class EstadoFinalizado extends EstadoBase {

    @Override
    public String getNombre() { return "FINALIZADO"; }

    @Override
    public Set<TransicionEvento> transicionesPosibles() {
        return EnumSet.noneOf(TransicionEvento.class);
    }

    @Override
    public void verificarModificable() {
        throw new TransicionInvalidaException("No se puede modificar un evento ya finalizado"); // CDU 014, 1.2
    }

    @Override
    public void verificarAdmiteCambios() {
        throw new TransicionInvalidaException("Evento ya finalizado"); // CDU 013, 1.2
    }

    @Override
    protected TransicionInvalidaException noPermitido(TransicionEvento transicion) {
        return new TransicionInvalidaException("Evento ya finalizado");
    }

    @Override
    public boolean ocupaLocacion() {
        return false;
    }
}
