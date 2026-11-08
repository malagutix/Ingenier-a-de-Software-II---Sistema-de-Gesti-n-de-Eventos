package modelo.eventos.entidades;

import java.time.LocalTime;

/** Datos del formulario de tipos de evento. */
public final class DatosTipoEvento {

    private final String nombre;
    private final Integer duracionSugeridaHoras;
    private final LocalTime horaInicioSugerida;
    private final String requerimientosSugeridos;

    public DatosTipoEvento(String nombre, Integer duracionSugeridaHoras, LocalTime horaInicioSugerida,
                           String requerimientosSugeridos) {
        this.nombre = nombre;
        this.duracionSugeridaHoras = duracionSugeridaHoras;
        this.horaInicioSugerida = horaInicioSugerida;
        this.requerimientosSugeridos = requerimientosSugeridos;
    }

    public String nombre() { return nombre; }
    public Integer duracionSugeridaHoras() { return duracionSugeridaHoras; }
    public LocalTime horaInicioSugerida() { return horaInicioSugerida; }
    public String requerimientosSugeridos() { return requerimientosSugeridos; }
}
