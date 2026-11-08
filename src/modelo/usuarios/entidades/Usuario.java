package modelo.usuarios.entidades;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import modelo.usuarios.entidades.rol.Rol;
import modelo.usuarios.entidades.rol.RolPendiente;
import modelo.usuarios.seguridad.HashContrasena;

/**
 * Usuario del sistema. Ya no es abstracta: el comportamiento que antes daban
 * las subclases Organizador y Administrador ahora lo da el objeto Rol.
 * Implementa Observer para recibir notificaciones de los eventos.
 */
public class Usuario implements Observer {

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String dni;
    private String correoElectronico;
    private String contrasenaHash;
    private Rol rol;
    private final List<Notificacion> notificaciones = new ArrayList<>();

    public Usuario(String nombre, String apellido, String dni,
                   String correoElectronico, String contrasenaHash) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correoElectronico = correoElectronico;
        this.contrasenaHash = contrasenaHash;
        this.rol = new RolPendiente();
    }

    public boolean tienePermiso(Permiso permiso) {
        return rol.puede(permiso);
    }

    public void cambiarRol(Rol nuevoRol) {
        this.rol = nuevoRol;
    }

    public boolean verificarContrasena(String contrasenaIngresada) {
        return HashContrasena.verificar(contrasenaIngresada, contrasenaHash);
    }

    @Override
    public void actualizar(String mensaje) {
        notificaciones.add(new Notificacion(mensaje));
    }

    /** Notificaciones en orden de llegada. */
    public List<Notificacion> getNotificaciones() {
        return Collections.unmodifiableList(notificaciones);
    }

    public int getCantidadNoLeidas() {
        return (int) notificaciones.stream().filter(n -> !n.isLeida()).count();
    }

    /** Busca, entre las notificaciones del usuario, las que corresponden a las indicadas. */
    public List<Notificacion> buscarNotificaciones(Collection<Notificacion> indicadas) {
        List<Notificacion> encontradas = new ArrayList<>();
        for (Notificacion n : notificaciones) {
            if (indicadas.stream().anyMatch(n::esLaMisma)) {
                encontradas.add(n);
            }
        }
        return encontradas;
    }

    public void quitarNotificaciones(Collection<Notificacion> eliminadas) {
        notificaciones.removeAll(eliminadas);
    }

    /** Reemplaza las notificaciones por las guardadas en la base de datos (solo para la persistencia). */
    public void restaurarNotificaciones(List<Notificacion> guardadas) {
        notificaciones.clear();
        notificaciones.addAll(guardadas);
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getContrasenaHash() { return contrasenaHash; }
    public Rol getRol() { return rol; }

    /** Lo asigna el repositorio al guardar el usuario por primera vez. */
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setDni(String dni) { this.dni = dni; }
    public void setCorreoElectronico(String correo) { this.correoElectronico = correo; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    @Override
    public String toString() {
        return String.format("#%d %s %s | DNI %s | %s | rol %s",
                idUsuario, nombre, apellido, dni, correoElectronico, rol.getNombre());
    }
}
