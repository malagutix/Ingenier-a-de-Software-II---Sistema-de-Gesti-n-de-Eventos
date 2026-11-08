package modelo.eventos.entidades;

/** Datos del formulario de locaciones. */
public final class DatosLocacion {

    private final String nombre;
    private final String direccion;
    private final TipoLocacion tipo;
    private final Integer capacidad;
    private final Double distanciaKm;
    private final Integer tiempoTrasladoMin;
    private final Integer tiempoMontajeMin;
    private final Integer vehiculosRequeridos;
    private final boolean requiereCamaraFrio;
    private final String otrosRequerimientos;

    public DatosLocacion(String nombre, String direccion, TipoLocacion tipo, Integer capacidad, Double distanciaKm,
                         Integer tiempoTrasladoMin, Integer tiempoMontajeMin, Integer vehiculosRequeridos,
                         boolean requiereCamaraFrio, String otrosRequerimientos) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.distanciaKm = distanciaKm;
        this.tiempoTrasladoMin = tiempoTrasladoMin;
        this.tiempoMontajeMin = tiempoMontajeMin;
        this.vehiculosRequeridos = vehiculosRequeridos;
        this.requiereCamaraFrio = requiereCamaraFrio;
        this.otrosRequerimientos = otrosRequerimientos;
    }

    public String nombre() { return nombre; }
    public String direccion() { return direccion; }
    public TipoLocacion tipo() { return tipo; }
    public Integer capacidad() { return capacidad; }
    public Double distanciaKm() { return distanciaKm; }
    public Integer tiempoTrasladoMin() { return tiempoTrasladoMin; }
    public Integer tiempoMontajeMin() { return tiempoMontajeMin; }
    public Integer vehiculosRequeridos() { return vehiculosRequeridos; }
    public boolean requiereCamaraFrio() { return requiereCamaraFrio; }
    public String otrosRequerimientos() { return otrosRequerimientos; }
}
