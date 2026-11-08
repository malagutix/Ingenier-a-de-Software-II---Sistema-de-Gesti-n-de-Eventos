package modelo.eventos.entidades;

import java.time.LocalDate;

/** Período en el que una locación no está disponible (mantenimiento, uso propio, etc.). */
public class BloqueoFecha {

    private final LocalDate desde;
    private final LocalDate hasta;
    private final String motivo;

    public BloqueoFecha(LocalDate desde, LocalDate hasta, String motivo) {
        this.desde = desde;
        this.hasta = hasta;
        this.motivo = motivo;
    }

    public boolean incluye(LocalDate fecha) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    public LocalDate getDesde() { return desde; }
    public LocalDate getHasta() { return hasta; }
    public String getMotivo() { return motivo; }

    /** Dos bloqueos son iguales si tienen las mismas fechas y el mismo motivo. */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BloqueoFecha)) {
            return false;
        }
        BloqueoFecha otro = (BloqueoFecha) o;
        return desde.equals(otro.desde) && hasta.equals(otro.hasta) && motivo.equals(otro.motivo);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(desde, hasta, motivo);
    }

    @Override
    public String toString() {
        String rango = desde.equals(hasta) ? Formato.fecha(desde) : Formato.fecha(desde) + " al " + Formato.fecha(hasta);
        return rango + " - " + motivo;
    }
}
