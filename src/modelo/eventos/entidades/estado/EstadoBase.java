package modelo.eventos.entidades.estado;

import java.util.Set;

import modelo.eventos.excepciones.TransicionInvalidaException;

/**
 * Comportamiento por defecto: ninguna transición está permitida.
 * Cada estado concreto sobrescribe solo lo que sí puede hacer.
 */
abstract class EstadoBase implements EstadoEvento {

    @Override
    public EstadoEvento aceptar() { throw noPermitido(TransicionEvento.ACEPTAR); }

    @Override
    public EstadoEvento iniciar() { throw noPermitido(TransicionEvento.INICIAR); }

    @Override
    public EstadoEvento finalizar() { throw noPermitido(TransicionEvento.FINALIZAR); }

    @Override
    public EstadoEvento reprogramar() { throw noPermitido(TransicionEvento.REPROGRAMAR); }

    @Override
    public EstadoEvento cancelar() { throw noPermitido(TransicionEvento.CANCELAR); }

    @Override
    public void verificarModificable() {
        // por defecto se puede modificar
    }

    @Override
    public void verificarEliminable(boolean fueAceptado) {
        // por defecto se puede eliminar
    }

    @Override
    public void verificarAdmiteCambios() {
        if (transicionesPosibles().isEmpty()) {
            throw new TransicionInvalidaException("El evento " + getNombre() + " no admite cambios de estado");
        }
    }

    @Override
    public boolean esFinal() {
        return transicionesPosibles().isEmpty();
    }

    @Override
    public boolean ocupaLocacion() {
        return true;
    }

    @Override
    public abstract Set<TransicionEvento> transicionesPosibles();

    protected TransicionInvalidaException noPermitido(TransicionEvento transicion) {
        return new TransicionInvalidaException(
                "No se puede " + transicion.getVerbo() + " un evento en estado " + getNombre());
    }

    @Override
    public String toString() {
        return getNombre();
    }
}
