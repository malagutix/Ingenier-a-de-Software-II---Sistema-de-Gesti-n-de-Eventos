package modelo.usuarios.entidades.rol;

import java.util.EnumSet;
import java.util.Set;

import modelo.usuarios.entidades.Permiso;

/** El organizador crea, modifica y valida eventos y genera sus hojas de servicio (CDU 011 a 015). */
public class RolOrganizador implements Rol {

    private static final Set<Permiso> PERMISOS = EnumSet.of(
            Permiso.ACCEDER_SISTEMA,
            Permiso.VER_CALENDARIO,
            Permiso.GESTIONAR_EVENTOS,
            Permiso.VALIDAR_EVENTO,
            Permiso.GENERAR_HOJA_SERVICIO);

    @Override
    public String getNombre() {
        return "ORGANIZADOR";
    }

    @Override
    public boolean puede(Permiso permiso) {
        return PERMISOS.contains(permiso);
    }
}
