package modelo.eventos.entidades.estado;

/** Acciones que cambian el estado de un evento (CDU 013 - Validar evento). */
public enum TransicionEvento {
    ACEPTAR("Aceptar", "aceptar"),
    INICIAR("Iniciar", "iniciar"),
    FINALIZAR("Finalizar", "finalizar"),
    REPROGRAMAR("Reprogramar", "reprogramar"),
    CANCELAR("Cancelar", "cancelar");

    private final String etiqueta;
    private final String verbo;

    TransicionEvento(String etiqueta, String verbo) {
        this.etiqueta = etiqueta;
        this.verbo = verbo;
    }

    public String getVerbo() { return verbo; }

    @Override
    public String toString() {
        return etiqueta;
    }
}
