package modelo.usuarios.entidades.rol;

import modelo.usuarios.entidades.Permiso;

/** El administrador puede realizar todas las acciones del sistema. */
public class RolAdministrador implements Rol {

    @Override
    public String getNombre() {
        return "ADMINISTRADOR";
    }

    @Override
    public boolean puede(Permiso permiso) {
        return true;
    }
}
