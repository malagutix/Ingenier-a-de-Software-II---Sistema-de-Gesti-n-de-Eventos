package modelo.eventos.entidades;

import java.time.LocalDateTime;

/** Período de tiempo [desde, hasta) en que una locación está ocupada. */
public final class Intervalo {

    private final LocalDateTime desde;
    private final LocalDateTime hasta;

    public Intervalo(LocalDateTime desde, LocalDateTime hasta) {
        this.desde = desde;
        this.hasta = hasta;
    }

    public boolean seSuperponeCon(Intervalo otro) {
        return desde.isBefore(otro.hasta) && otro.desde.isBefore(hasta);
    }

    public LocalDateTime getDesde() { return desde; }
    public LocalDateTime getHasta() { return hasta; }
}
