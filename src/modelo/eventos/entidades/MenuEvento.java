package modelo.eventos.entidades;

/**
 * Menú requerido en la solicitud (RF008). El catálogo de menús y sus precios
 * pertenecen al Subsistema 2; acá solo se registra la elección del cliente.
 */
public enum MenuEvento {
    ESTANDAR("Menú estándar"),
    VEGETARIANO("Menú vegetariano"),
    SIN_TACC("Menú sin TACC"),
    INFANTIL("Menú infantil"),
    LUNCH("Lunch / catering"),
    SIN_MENU("Sin menú");

    private final String descripcion;

    MenuEvento(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
