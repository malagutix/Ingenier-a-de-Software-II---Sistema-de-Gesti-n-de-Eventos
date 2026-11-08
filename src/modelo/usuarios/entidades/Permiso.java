package modelo.usuarios.entidades;

/**
 * Acciones del sistema que dependen del rol del usuario.
 * Cada Rol indica cuáles de estas acciones puede realizar.
 */
public enum Permiso {
    ACCEDER_SISTEMA("Ingresar al sistema"),
    VER_CALENDARIO("Ver calendario"),
    GESTIONAR_USUARIOS("Gestionar usuarios"),
    ASIGNAR_ROL("Asignar roles"),
    GESTIONAR_BD("Gestionar base de datos"),
    GESTIONAR_SOLICITUDES("Gestionar solicitudes"),
    GESTIONAR_EVENTOS("Gestionar eventos"),
    VALIDAR_EVENTO("Validar eventos"),
    GENERAR_HOJA_SERVICIO("Generar hoja de servicio"),
    GESTIONAR_LOCACIONES("Gestionar locaciones"),
    GESTIONAR_TIPOS_EVENTO("Gestionar tipos de evento");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
