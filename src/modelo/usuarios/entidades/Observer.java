package modelo.usuarios.entidades;

/**
 * Patrón Observer: interfaz que implementan los objetos que quieren ser
 * notificados cuando cambia algo relevante (por ejemplo, un Evento).
 * El Evento del otro módulo tendrá agregarObserver(), quitarObserver() y notificar().
 */
public interface Observer {
    void actualizar(String mensaje);
}
