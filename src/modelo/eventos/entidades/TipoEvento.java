package modelo.eventos.entidades;

import java.time.LocalTime;

/**
 * Tipo de evento (congreso, feria, boda...). Es administrable desde el sistema
 * para poder incorporar tipos nuevos sin modificar el código (requisito de
 * extensibilidad del enunciado). EventoFactory usa sus valores sugeridos.
 */
public class TipoEvento {

    private int idTipoEvento;
    private String nombre;
    private int duracionSugeridaHoras;
    private LocalTime horaInicioSugerida;
    private String requerimientosSugeridos;
    private boolean activo = true;

    public TipoEvento(String nombre, int duracionSugeridaHoras, LocalTime horaInicioSugerida,
                      String requerimientosSugeridos) {
        modificar(nombre, duracionSugeridaHoras, horaInicioSugerida, requerimientosSugeridos);
    }

    public final void modificar(String nombre, int duracionSugeridaHoras, LocalTime horaInicioSugerida,
                                String requerimientosSugeridos) {
        this.nombre = nombre;
        this.duracionSugeridaHoras = duracionSugeridaHoras;
        this.horaInicioSugerida = horaInicioSugerida;
        this.requerimientosSugeridos = requerimientosSugeridos;
    }

    public void activar() { this.activo = true; }
    public void desactivar() { this.activo = false; }

    public int getIdTipoEvento() { return idTipoEvento; }
    public void setIdTipoEvento(int id) { this.idTipoEvento = id; }
    public String getNombre() { return nombre; }
    public int getDuracionSugerida() { return duracionSugeridaHoras; }
    public LocalTime getHoraInicioSugerida() { return horaInicioSugerida; }
    public String getRequerimientosSugeridos() { return requerimientosSugeridos; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() {
        return nombre;
    }
}
