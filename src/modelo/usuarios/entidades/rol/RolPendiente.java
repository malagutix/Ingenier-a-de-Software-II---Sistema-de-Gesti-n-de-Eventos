package modelo.usuarios.entidades.rol;

import modelo.usuarios.entidades.Permiso;

/**
 * Rol de un usuario recién registrado al que el administrador todavía no le
 * asignó un rol. No tiene ningún permiso, ni siquiera para ingresar (RF005).
 */
public class RolPendiente implements Rol {

    @Override
    public String getNombre() {
        return "PENDIENTE";
    }

    @Override
    public boolean puede(Permiso permiso) {
        return false;
    }
}
