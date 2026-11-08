package modelo.usuarios.entidades.rol;

import modelo.usuarios.entidades.Permiso;

/**
 * Patrón Strategy: cada implementación define qué puede hacer un usuario.
 * El usuario tiene un Rol que se puede reemplazar en tiempo de ejecución (RF005).
 */
public interface Rol {
    String getNombre();

    boolean puede(Permiso permiso);
}
