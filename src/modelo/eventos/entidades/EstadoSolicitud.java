package modelo.eventos.entidades;

/**
 * Estado de una solicitud. Nace PENDIENTE (CDU 008, paso 4); el administrador
 * la ACEPTA o la RECHAZA mientras no tenga eventos. Solo una solicitud
 * ACEPTADA se puede planificar. ELIMINADA es una solicitud que se quitó de la
 * lista pero se conserva porque sus eventos siguen vigentes (CDU 010).
 */
public enum EstadoSolicitud {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA,
    ELIMINADA
}
