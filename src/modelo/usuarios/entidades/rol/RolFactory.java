package modelo.usuarios.entidades.rol;

import modelo.usuarios.excepciones.DatosInvalidosException;

/**
 * Patrón Factory Method: único lugar donde se crean los roles.
 * Se usa cuando el administrador elige un rol por nombre y cuando se
 * reconstruye el rol guardado en la base de datos.
 * Para agregar un rol nuevo solo hay que crear la clase y sumarla acá.
 */
public class RolFactory {

    public Rol crearRol(String nombre) {
        if (nombre == null) {
            throw new DatosInvalidosException("Debe indicar un rol");
        }
        switch (nombre.trim().toUpperCase()) {
            case "ADMINISTRADOR":
                return new RolAdministrador();
            case "ORGANIZADOR":
                return new RolOrganizador();
            case "CLIENTE":
                return new RolCliente();
            case "PENDIENTE":
                return new RolPendiente();
            default:
                throw new DatosInvalidosException("Rol inexistente: " + nombre);
        }
    }
}
