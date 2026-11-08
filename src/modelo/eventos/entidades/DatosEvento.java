package modelo.eventos.entidades;

import java.time.LocalTime;

/** Datos que el organizador puede cambiar de un evento (RF014). La fecha se cambia reprogramando. */
public final class DatosEvento {

    private final Integer idTipoEvento;
    private final LocalTime horaInicio;
    private final Integer duracionHoras;
    private final Integer cantidadAsistentes;
    private final Integer idLocacion;
    private final String requerimientosEspecificos;

    public DatosEvento(Integer idTipoEvento, LocalTime horaInicio, Integer duracionHoras, Integer cantidadAsistentes,
                       Integer idLocacion, String requerimientosEspecificos) {
        this.idTipoEvento = idTipoEvento;
        this.horaInicio = horaInicio;
        this.duracionHoras = duracionHoras;
        this.cantidadAsistentes = cantidadAsistentes;
        this.idLocacion = idLocacion;
        this.requerimientosEspecificos = requerimientosEspecificos;
    }

    public Integer idTipoEvento() { return idTipoEvento; }
    public LocalTime horaInicio() { return horaInicio; }
    public Integer duracionHoras() { return duracionHoras; }
    public Integer cantidadAsistentes() { return cantidadAsistentes; }
    public Integer idLocacion() { return idLocacion; }
    public String requerimientosEspecificos() { return requerimientosEspecificos; }
}
