package modelo.persistencia;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

/** Resumen del estado de la base de datos para la pantalla del CDU 007. */
public final class EstadoBaseDatos {

    private final ConfiguracionBaseDatos config;
    private final BaseDeDatos.Inicio inicio;
    private final LocalDateTime ultimoRespaldo;
    private final Path carpetaRespaldos;
    private final Map<String, Long> registros;

    EstadoBaseDatos(ConfiguracionBaseDatos config, BaseDeDatos.Inicio inicio, LocalDateTime ultimoRespaldo,
                    Path carpetaRespaldos, Map<String, Long> registros) {
        this.config = config;
        this.inicio = inicio;
        this.ultimoRespaldo = ultimoRespaldo;
        this.carpetaRespaldos = carpetaRespaldos;
        this.registros = registros;
    }

    public String getServidor() { return config.getServidor() + ":" + config.getPuerto(); }
    public String getBase() { return config.getBase(); }
    public String getUsuario() { return config.getUsuario(); }
    public String getVersionServidor() { return inicio == null ? "-" : inicio.getVersionServidor(); }
    public boolean isBaseCreadaAlIniciar() { return inicio != null && inicio.isBaseCreada(); }
    public LocalDateTime getUltimoRespaldo() { return ultimoRespaldo; }
    public Path getCarpetaRespaldos() { return carpetaRespaldos; }
    public Map<String, Long> getRegistros() { return registros; }
}
