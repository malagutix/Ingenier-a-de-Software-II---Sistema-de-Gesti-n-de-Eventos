package modelo.eventos.entidades;

/** Tipos de locación que menciona el enunciado. */
public enum TipoLocacion {
    SALON_PROPIO("Salón propio"),
    ESPACIO_ALQUILADO("Espacio alquilado"),
    DOMICILIO_PARTICULAR("Domicilio particular"),
    ESPACIO_PUBLICO("Espacio público");

    private final String descripcion;

    TipoLocacion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
