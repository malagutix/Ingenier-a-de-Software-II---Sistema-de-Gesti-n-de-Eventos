package modelo.eventos.servicio;

import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.excepciones.AccesoDenegadoException;
import modelo.usuarios.servicio.GestorUsuarios;

/** Verifica sesión activa y permisos antes de cada operación (RNF001). */
class ControlAcceso {

    private final GestorUsuarios gestorUsuarios;

    ControlAcceso(GestorUsuarios gestorUsuarios) {
        this.gestorUsuarios = gestorUsuarios;
    }

    void verificar(Usuario usuario, Permiso permiso) {
        if (usuario == null || !gestorUsuarios.tieneSesionActiva(usuario)) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta acción");
        }
        if (!usuario.tienePermiso(permiso)) {
            throw new AccesoDenegadoException("No tiene permisos para realizar esta acción");
        }
    }
}
