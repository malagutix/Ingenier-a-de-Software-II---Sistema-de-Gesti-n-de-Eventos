package modelo.usuarios.entidades.rol;

import java.util.EnumSet;
import java.util.Set;

import modelo.usuarios.entidades.Permiso;

/** El cliente puede ingresar al sistema y consultar el calendario. */
public class RolCliente implements Rol {

    private static final Set<Permiso> PERMISOS = EnumSet.of(
            Permiso.ACCEDER_SISTEMA,
            Permiso.VER_CALENDARIO);

    @Override
    public String getNombre() {
        return "CLIENTE";
    }

    @Override
    public boolean puede(Permiso permiso) {
        return PERMISOS.contains(permiso);
    }
}
