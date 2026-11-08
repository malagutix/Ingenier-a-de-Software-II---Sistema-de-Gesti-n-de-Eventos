package modelo.usuarios.servicio;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import modelo.persistencia.UnidadDeTrabajo;
import modelo.usuarios.entidades.DatosUsuario;
import modelo.usuarios.entidades.Notificacion;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.TokenRecuperacion;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.entidades.rol.Rol;
import modelo.usuarios.entidades.rol.RolFactory;
import modelo.usuarios.excepciones.AccesoDenegadoException;
import modelo.usuarios.excepciones.AutenticacionException;
import modelo.usuarios.excepciones.DatosInvalidosException;
import modelo.usuarios.excepciones.UsuarioNoEncontradoException;
import modelo.usuarios.repositorio.TokenRepository;
import modelo.usuarios.repositorio.TokenRepositoryEnMemoria;
import modelo.usuarios.repositorio.UsuarioRepository;
import modelo.usuarios.seguridad.HashContrasena;

/**
 * Patrón Facade: única puerta de entrada al módulo de usuarios.
 * Las pantallas (Login, PantallaUsuarios) solo conocen esta clase.
 * También es quien controla la autenticación y los permisos (RNF001).
 *
 * Casos de uso cubiertos: CDU 001 a CDU 006.
 */
public class GestorUsuarios {

    private final UsuarioRepository repositorio;
    private final RolFactory rolFactory;
    private final ServicioCorreo servicioCorreo;
    private final ValidadorUsuario validador = new ValidadorUsuario();

    private final Set<Integer> sesionesActivas = new HashSet<>();
    private final TokenRepository tokens;
    private final UnidadDeTrabajo tx;
    /** Indica si un usuario tiene solicitudes o eventos asociados (no se puede eliminar). */
    private IntPredicate tieneReferencias = id -> false;

    public boolean correoEsReal() {
        return servicioCorreo.esReal();
    }

    /** Versión en memoria (sin base de datos). */
    public GestorUsuarios(UsuarioRepository repositorio, RolFactory rolFactory,
                          ServicioCorreo servicioCorreo) {
        this(repositorio, rolFactory, servicioCorreo, new TokenRepositoryEnMemoria(), UnidadDeTrabajo.SIN_TRANSACCION);
    }

    public GestorUsuarios(UsuarioRepository repositorio, RolFactory rolFactory, ServicioCorreo servicioCorreo,
                          TokenRepository tokens, UnidadDeTrabajo tx) {
        this.repositorio = repositorio;
        this.rolFactory = rolFactory;
        this.servicioCorreo = servicioCorreo;
        this.tokens = tokens;
        this.tx = tx;
    }

    /** Lo configura Main: consulta si el usuario figura en solicitudes o eventos. */
    public void setVerificadorDeReferencias(IntPredicate tieneReferencias) {
        this.tieneReferencias = tieneReferencias;
    }

    /** Indica si todavía no hay usuarios (primera ejecución). */
    public boolean sinUsuarios() {
        return repositorio.listarTodos().isEmpty();
    }

    /** Notificaciones actualizadas del usuario (pueden haber llegado desde otra computadora). */
    public List<Notificacion> obtenerNotificaciones(Usuario usuario) {
        return actualizado(usuario).getNotificaciones();
    }

    /** Cantidad de notificaciones que el usuario todavía no leyó. */
    public int contarNoLeidas(Usuario usuario) {
        return actualizado(usuario).getCantidadNoLeidas();
    }

    /** Marca como leídas las notificaciones indicadas del usuario. */
    public void marcarNotificacionesLeidas(Usuario usuario, List<Notificacion> notificaciones) {
        tx.ejecutar((Runnable) () -> {
            Usuario u = actualizado(usuario);
            List<Notificacion> propias = u.buscarNotificaciones(notificaciones);
            propias.forEach(Notificacion::marcarLeida);
            repositorio.marcarNotificacionesLeidas(u, propias);
        });
    }

    /** Elimina las notificaciones indicadas del usuario. */
    public void eliminarNotificaciones(Usuario usuario, List<Notificacion> notificaciones) {
        tx.ejecutar((Runnable) () -> {
            Usuario u = actualizado(usuario);
            List<Notificacion> propias = u.buscarNotificaciones(notificaciones);
            u.quitarNotificaciones(propias);
            repositorio.eliminarNotificaciones(u, propias);
        });
    }

    /** El usuario como está guardado (sus notificaciones pueden haber llegado desde otra computadora). */
    private Usuario actualizado(Usuario usuario) {
        return repositorio.buscarPorId(usuario.getIdUsuario()).orElse(usuario);
    }

    // ------------------------------------------------------------------
    // Puesta en marcha
    // ------------------------------------------------------------------

    /**
     * Crea el primer administrador. Solo funciona si todavía no hay ninguno,
     * porque sin un administrador nadie podría asignar roles.
     */
    public Usuario crearAdministradorInicial(DatosUsuario datos) {
        return tx.ejecutar((Supplier<Usuario>) () -> {
            boolean hayAdmin = repositorio.listarTodos().stream()
                    .anyMatch(u -> u.tienePermiso(Permiso.ASIGNAR_ROL));
            if (hayAdmin) {
                throw new AccesoDenegadoException("Ya existe un administrador en el sistema");
            }
            Usuario admin = registrarUsuario(datos);
            admin.cambiarRol(rolFactory.crearRol("ADMINISTRADOR"));
            repositorio.actualizar(admin);
            return admin;
        });
    }

    // ------------------------------------------------------------------
    // CDU 001 - Registro de usuario (RF001)
    // ------------------------------------------------------------------

    public Usuario registrarUsuario(DatosUsuario datos) {
        return tx.ejecutar((Supplier<Usuario>) () -> {
            validador.validarNombre(datos.nombre(), "Nombre");
            validador.validarNombre(datos.apellido(), "Apellido");
            validador.validarDni(datos.dni());
            validador.validarCorreo(datos.correoElectronico());
            validador.validarContrasena(datos.contrasena());

            String dni = datos.dni().trim();
            String correo = normalizarCorreo(datos.correoElectronico());

            if (repositorio.buscarPorDni(dni).isPresent()) {
                throw new DatosInvalidosException("El DNI ya se encuentra registrado");
            }
            if (repositorio.buscarPorCorreo(correo).isPresent()) {
                throw new DatosInvalidosException("El correo electrónico ya se encuentra registrado");
            }

            Usuario usuario = new Usuario(
                    datos.nombre().trim(),
                    datos.apellido().trim(),
                    dni,
                    correo,
                    HashContrasena.generar(datos.contrasena()));

            repositorio.guardar(usuario); // queda con RolPendiente hasta que el admin le asigne uno
            return usuario;
        });
    }

    // ------------------------------------------------------------------
    // CDU 002 - Iniciar sesión (RF002)
    // ------------------------------------------------------------------

    public Usuario iniciarSesion(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            throw new AutenticacionException("Error de autenticación");
        }
        Usuario usuario = repositorio.buscarPorCorreo(normalizarCorreo(correo))
                .filter(u -> u.verificarContrasena(contrasena))
                // Mismo mensaje si falla el correo o la contraseña, para no revelar cuál existe
                .orElseThrow(() -> new AutenticacionException("Error de autenticación"));

        if (!usuario.tienePermiso(Permiso.ACCEDER_SISTEMA)) {
            throw new AccesoDenegadoException(
                    "Tu cuenta todavía no tiene un rol asignado. Contactá al administrador");
        }
        sesionesActivas.add(usuario.getIdUsuario());
        return usuario;
    }

    public void cerrarSesion(Usuario usuario) {
        sesionesActivas.remove(usuario.getIdUsuario());
    }

    public boolean tieneSesionActiva(Usuario usuario) {
        return sesionesActivas.contains(usuario.getIdUsuario());
    }

    // ------------------------------------------------------------------
    // CDU 003 - Restablecer contraseña (RF003)
    // ------------------------------------------------------------------

    /** Paso 1: el usuario ingresa su correo y recibe un código de recuperación. */
    public void solicitarRecuperacion(String correo) {
        validador.validarCorreo(correo);
        Usuario usuario = repositorio.buscarPorCorreo(normalizarCorreo(correo))
                .orElseThrow(() -> new UsuarioNoEncontradoException());

        TokenRecuperacion token = new TokenRecuperacion(usuario);
        servicioCorreo.enviarRecuperacion(usuario.getCorreoElectronico(), token.getCodigo());
        tokens.guardar(token); // solo si el envío salió bien
    }

    /** Paso 2: el usuario ingresa el código recibido y la nueva contraseña dos veces. */
    /** @return el correo de la cuenta cuya contraseña se cambió, para confirmárselo al usuario */
    public String restablecerContrasena(String codigo, String nueva, String confirmacion) {
        return tx.ejecutar((Supplier<String>) () -> {
            TokenRecuperacion recuperacion = tokens.buscarPorCodigo(TokenRecuperacion.normalizar(codigo)).orElse(null);
            if (recuperacion == null || !recuperacion.esValido()) {
                throw new AutenticacionException("El código de recuperación no es válido o venció");
            }
            if (nueva == null || !nueva.equals(confirmacion)) {
                throw new DatosInvalidosException("Contraseñas no coinciden");
            }
            validador.validarContrasena(nueva);

            Usuario usuario = recuperacion.getUsuario();
            usuario.setContrasenaHash(HashContrasena.generar(nueva));
            repositorio.actualizar(usuario);
            recuperacion.marcarUsado();
            tokens.actualizar(recuperacion);
            sesionesActivas.remove(usuario.getIdUsuario()); // cierra sesiones abiertas con la contraseña vieja
            return usuario.getCorreoElectronico();
        });
    }

    // ------------------------------------------------------------------
    // CDU 004 - Gestionar usuario y contraseña (RF004)
    // ------------------------------------------------------------------

    /**
     * Busca usuarios por DNI, correo y/o rol. Los criterios en null se ignoran.
     */
    public List<Usuario> buscarUsuarios(Usuario admin, String dni, String correo, String rol) {
        verificarPermiso(admin, Permiso.GESTIONAR_USUARIOS);
        List<Usuario> resultado = repositorio.listarTodos().stream()
                .filter(u -> dni == null || u.getDni().equals(dni.trim()))
                .filter(u -> correo == null || u.getCorreoElectronico().equalsIgnoreCase(correo.trim()))
                .filter(u -> rol == null || u.getRol().getNombre().equalsIgnoreCase(rol.trim()))
                .collect(Collectors.toList());
        if (resultado.isEmpty()) {
            throw new UsuarioNoEncontradoException();
        }
        return resultado;
    }

    public void eliminarUsuario(Usuario admin, int idUsuario) {
        tx.ejecutar((Runnable) () -> {
            verificarPermiso(admin, Permiso.GESTIONAR_USUARIOS);
            Usuario usuario = obtener(idUsuario);
            if (usuario.getIdUsuario() == admin.getIdUsuario()) {
                throw new AccesoDenegadoException("El administrador no puede eliminarse a sí mismo");
            }
            if (tieneReferencias.test(idUsuario)) {
                throw new DatosInvalidosException("No se puede eliminar a " + usuario.getNombre() + " "
                        + usuario.getApellido() + ": tiene solicitudes o eventos asociados");
            }
            repositorio.eliminar(idUsuario);
            sesionesActivas.remove(idUsuario);
        });
    }

    // ------------------------------------------------------------------
    // CDU 005 - Asignar roles (RF005)
    // ------------------------------------------------------------------

    public void asignarRol(Usuario admin, int idUsuario, String nombreRol) {
        tx.ejecutar((Runnable) () -> {
            verificarPermiso(admin, Permiso.ASIGNAR_ROL);
            Usuario usuario = obtener(idUsuario);
            if (usuario.getIdUsuario() == admin.getIdUsuario()) {
                throw new AccesoDenegadoException("El administrador no puede cambiar su propio rol");
            }
            Rol nuevoRol = rolFactory.crearRol(nombreRol);
            usuario.cambiarRol(nuevoRol); // único rol activo: reemplaza al anterior
            repositorio.actualizar(usuario);
        });
    }

    // ------------------------------------------------------------------
    // CDU 006 - Modificar usuario (RF006)
    // ------------------------------------------------------------------

    /**
     * Modifica los datos del usuario. Los campos de DatosUsuario que vengan
     * en null o vacíos quedan como estaban.
     */
    public Usuario modificarUsuario(Usuario admin, int idUsuario, DatosUsuario nuevos) {
        return tx.ejecutar((Supplier<Usuario>) () -> {
            verificarPermiso(admin, Permiso.GESTIONAR_USUARIOS);
            Usuario usuario = obtener(idUsuario);

            // Primero se valida todo; recién después se modifica, así no queda a medias
            if (tieneValor(nuevos.nombre())) validador.validarNombre(nuevos.nombre(), "Nombre");
            if (tieneValor(nuevos.apellido())) validador.validarNombre(nuevos.apellido(), "Apellido");
            if (tieneValor(nuevos.contrasena())) validador.validarContrasena(nuevos.contrasena());

            if (tieneValor(nuevos.dni())) {
                validador.validarDni(nuevos.dni());
                repositorio.buscarPorDni(nuevos.dni().trim())
                        .filter(otro -> otro.getIdUsuario() != idUsuario)
                        .ifPresent(otro -> {
                            throw new DatosInvalidosException("El DNI ya pertenece a otro usuario");
                        });
            }
            if (tieneValor(nuevos.correoElectronico())) {
                validador.validarCorreo(nuevos.correoElectronico());
                repositorio.buscarPorCorreo(normalizarCorreo(nuevos.correoElectronico()))
                        .filter(otro -> otro.getIdUsuario() != idUsuario)
                        .ifPresent(otro -> {
                            throw new DatosInvalidosException("El correo electrónico ya pertenece a otro usuario");
                        });
            }

            if (tieneValor(nuevos.nombre())) usuario.setNombre(nuevos.nombre().trim());
            if (tieneValor(nuevos.apellido())) usuario.setApellido(nuevos.apellido().trim());
            if (tieneValor(nuevos.dni())) usuario.setDni(nuevos.dni().trim());
            if (tieneValor(nuevos.correoElectronico()))
                usuario.setCorreoElectronico(normalizarCorreo(nuevos.correoElectronico()));
            if (tieneValor(nuevos.contrasena()))
                usuario.setContrasenaHash(HashContrasena.generar(nuevos.contrasena()));

            repositorio.actualizar(usuario);
            return usuario;
        });
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /** Solo usuarios autenticados y con el permiso necesario (RNF001). */
    private void verificarPermiso(Usuario usuario, Permiso permiso) {
        if (usuario == null || !tieneSesionActiva(usuario)) {
            throw new AccesoDenegadoException("Debe iniciar sesión para realizar esta acción");
        }
        if (!usuario.tienePermiso(permiso)) {
            throw new AccesoDenegadoException("No tiene permisos para realizar esta acción");
        }
    }

    private Usuario obtener(int idUsuario) {
        return repositorio.buscarPorId(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException());
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private static boolean tieneValor(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
