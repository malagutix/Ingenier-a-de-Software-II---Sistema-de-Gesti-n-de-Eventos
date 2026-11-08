package modelo.eventos.excepciones;

/**
 * La locación no tiene capacidad o ya está ocupada en esa fecha (RF008, RF009).
 * El mensaje incluye las locaciones alternativas que sí están disponibles (CDU 008, paso 3.2).
 */
public class DisponibilidadException extends EventoException {
    private static final long serialVersionUID = 1L;

    public DisponibilidadException(String mensaje) {
        super(mensaje);
    }
}
