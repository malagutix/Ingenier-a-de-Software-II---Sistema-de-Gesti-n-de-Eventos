package modelo.eventos.entidades.estado;

import java.util.EnumSet;
import java.util.Set;

/** Estado inicial de todo evento recién creado. */
public class EstadoPendienteDeReserva extends EstadoBase {

    @Override
    public String getNombre() { return "PENDIENTE DE RESERVA"; }

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
}
