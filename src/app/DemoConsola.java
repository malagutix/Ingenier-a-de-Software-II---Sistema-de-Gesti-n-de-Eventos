package app;

import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.entidades.rol.RolFactory;
import modelo.usuarios.excepciones.UsuarioException;
import modelo.usuarios.repositorio.UsuarioRepositoryEnMemoria;
import modelo.usuarios.servicio.GestorUsuarios;
import modelo.usuarios.servicio.ServicioCorreoConsola;

/**
 * Demostración por consola de los casos de uso CDU 001 a CDU 006.
 */
public class DemoConsola {

    public static void main(String[] args) {
        ServicioCorreoConsola correo = new ServicioCorreoConsola();
        GestorUsuarios gestor = new GestorUsuarios(
                new UsuarioRepositoryEnMemoria(), new RolFactory(), correo);

        titulo("Puesta en marcha: se crea el primer administrador");
        Usuario admin = gestor.crearAdministradorInicial(new DatosUsuario(
                "Ana", "Pérez", "30111222", "admin@eventos.com", "Admin2026"));
        ok("Creado: " + admin);

        titulo("CDU 001 - Registro de usuario");
        Usuario lucas = gestor.registrarUsuario(new DatosUsuario(
                "Lucas", "Saraví", "40222333", "lucas@mail.com", "clave1234"));
        ok("Usuario registrado correctamente: " + lucas);
        intentar("Registro con DNI repetido", () -> gestor.registrarUsuario(new DatosUsuario(
                "Otro", "Usuario", "40222333", "otro@mail.com", "clave1234")));
        intentar("Registro con contraseña débil", () -> gestor.registrarUsuario(new DatosUsuario(
                "Otro", "Usuario", "41000000", "otro@mail.com", "123")));

        titulo("CDU 002 - Iniciar sesión");
        intentar("Login sin rol asignado", () -> gestor.iniciarSesion("lucas@mail.com", "clave1234"));
        intentar("Login con contraseña incorrecta", () -> gestor.iniciarSesion("admin@eventos.com", "mala1234"));
        Usuario adminLogueado = gestor.iniciarSesion("admin@eventos.com", "Admin2026");
        ok("Sesión iniciada: " + adminLogueado.getNombre());

        titulo("CDU 005 - Asignar roles");
        gestor.asignarRol(adminLogueado, lucas.getIdUsuario(), "ORGANIZADOR");
        ok("El rol fue modificado correctamente: " + lucas);
        Usuario lucasLogueado = gestor.iniciarSesion("lucas@mail.com", "clave1234");
        ok("Ahora Lucas puede ingresar: " + lucasLogueado.getRol().getNombre());
        intentar("Un organizador intenta asignar roles",
                () -> gestor.asignarRol(lucasLogueado, admin.getIdUsuario(), "CLIENTE"));

        titulo("CDU 004 - Gestionar usuarios (buscar)");
        gestor.buscarUsuarios(adminLogueado, null, null, "ORGANIZADOR")
                .forEach(u -> ok("Encontrado: " + u));
        intentar("Búsqueda sin resultados",
                () -> gestor.buscarUsuarios(adminLogueado, "99999999", null, null));

        titulo("CDU 006 - Modificar usuario");
        gestor.modificarUsuario(adminLogueado, lucas.getIdUsuario(),
                new DatosUsuario(null, null, null, "lucas.saravi@mail.com", null));
        ok("Modificación exitosa: " + lucas);
        intentar("Modificar con un correo que ya usa otro usuario",
                () -> gestor.modificarUsuario(adminLogueado, lucas.getIdUsuario(),
                        new DatosUsuario(null, null, null, "admin@eventos.com", null)));

        titulo("CDU 003 - Restablecer contraseña");
        gestor.solicitarRecuperacion("lucas.saravi@mail.com");
        String token = correo.getUltimoCodigo();
        intentar("Contraseñas distintas",
                () -> gestor.restablecerContrasena(token, "nueva2026", "nueva2027"));
        gestor.restablecerContrasena(token, "nueva2026", "nueva2026");
        ok("Contraseña actualizada correctamente");
        intentar("Reusar el mismo código",
                () -> gestor.restablecerContrasena(token, "otra2026", "otra2026"));
        ok("Login con la nueva contraseña: "
                + gestor.iniciarSesion("lucas.saravi@mail.com", "nueva2026").getNombre());

        titulo("CDU 004 - Gestionar usuarios (eliminar)");
        intentar("El admin intenta eliminarse a sí mismo",
                () -> gestor.eliminarUsuario(adminLogueado, adminLogueado.getIdUsuario()));
        gestor.eliminarUsuario(adminLogueado, lucas.getIdUsuario());
        ok("Eliminación de usuario exitosa");

        titulo("Observer - notificación desde un evento");
        adminLogueado.actualizar("El evento 'Congreso 2026' cambió a ACEPTADO");
        ok("Notificaciones del admin: " + adminLogueado.getNotificaciones());

        gestor.cerrarSesion(adminLogueado);
        ok("Sesión cerrada");
    }

    private static void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }

    private static void ok(String texto) {
        System.out.println("  OK    " + texto);
    }

    private static void intentar(String descripcion, Runnable accion) {
        try {
            accion.run();
            System.out.println("  ???   " + descripcion + ": se esperaba un error y no lo hubo");
        } catch (UsuarioException e) {
            System.out.println("  ERROR " + descripcion + " -> " + e.getMessage());
        }
    }
}
