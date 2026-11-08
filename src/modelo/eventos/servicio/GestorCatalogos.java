package modelo.eventos.servicio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import modelo.eventos.entidades.BloqueoFecha;
import modelo.eventos.entidades.DatosLocacion;
import modelo.eventos.entidades.DatosTipoEvento;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Intervalo;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.excepciones.DatosEventoInvalidosException;
import modelo.eventos.excepciones.EventoException;
import modelo.eventos.excepciones.NoEncontradoException;
import modelo.eventos.repositorio.EventoRepository;
import modelo.eventos.repositorio.LocacionRepository;
import modelo.eventos.repositorio.SolicitudRepository;
import modelo.eventos.repositorio.TipoEventoRepository;
import modelo.eventos.servicio.ReglasPlanificacion.Ocupacion;
import modelo.persistencia.UnidadDeTrabajo;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.servicio.GestorUsuarios;

/**
 * Patrón Facade para los catálogos del subsistema: gestión de locaciones
 * (capacidad, disponibilidad, logística) y gestión de tipos de evento.
 * Las restricciones protegen lo ya planificado: no se puede bajar la capacidad
 * por debajo de una reserva, bloquear fechas ocupadas ni eliminar lo que está en uso.
 */
public class GestorCatalogos {

    private static final int MAX_NOMBRE = 80;
    private static final int MAX_TEXTO = 500;

    private final ControlAcceso acceso;
    private final LocacionRepository locaciones;
    private final TipoEventoRepository tipos;
    private final SolicitudRepository solicitudes;
    private final EventoRepository eventos;
    private final ReglasPlanificacion reglas;
    private final UnidadDeTrabajo tx;

    /** Versión en memoria (sin base de datos). */
    public GestorCatalogos(GestorUsuarios gestorUsuarios, LocacionRepository locaciones, TipoEventoRepository tipos,
                           SolicitudRepository solicitudes, EventoRepository eventos, ReglasPlanificacion reglas) {
        this(gestorUsuarios, locaciones, tipos, solicitudes, eventos, reglas, UnidadDeTrabajo.SIN_TRANSACCION);
    }

    public GestorCatalogos(GestorUsuarios gestorUsuarios, LocacionRepository locaciones, TipoEventoRepository tipos,
                           SolicitudRepository solicitudes, EventoRepository eventos, ReglasPlanificacion reglas,
                           UnidadDeTrabajo tx) {
        this.tx = tx;
        this.acceso = new ControlAcceso(gestorUsuarios);
        this.locaciones = locaciones;
        this.tipos = tipos;
        this.solicitudes = solicitudes;
        this.eventos = eventos;
        this.reglas = reglas;
    }

    // =================================================================
    // Gestión de locaciones
    // =================================================================

    public List<Locacion> listarLocaciones(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
        return locaciones.listarTodos().stream()
                .sorted(Comparator.comparing(Locacion::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public Locacion crearLocacion(Usuario actor, DatosLocacion datos) {
        return tx.ejecutar((Supplier<Locacion>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            validarLocacion(datos, null);
            Locacion locacion = new Locacion(datos.nombre().trim(), datos.direccion().trim(), datos.tipo(),
                    datos.capacidad(), datos.distanciaKm(), datos.tiempoTrasladoMin(), datos.tiempoMontajeMin(),
                    datos.vehiculosRequeridos(), datos.requiereCamaraFrio(), limpiar(datos.otrosRequerimientos()));
            locaciones.guardar(locacion);
            return locacion;
        });
    }

    public Locacion modificarLocacion(Usuario actor, int idLocacion, DatosLocacion datos) {
        return tx.ejecutar((Supplier<Locacion>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            Locacion locacion = obtenerLocacion(idLocacion);
            validarLocacion(datos, locacion);

            // La capacidad no puede quedar por debajo de una reserva activa
            List<String> excedidas = new ArrayList<>();
            for (Ocupacion o : ocupacionesActivas(locacion)) {
                if (o.getPersonas() > datos.capacidad()) {
                    excedidas.add(o.getDescripcion() + ": " + o.getPersonas() + " personas");
                }
            }
            if (!excedidas.isEmpty()) {
                throw new EventoException("No se puede bajar la capacidad a " + datos.capacidad()
                        + " porque hay reservas con más personas:\n  - " + String.join("\n  - ", excedidas));
            }
            // Un tiempo de montaje mayor no puede hacer que se superpongan eventos ya planificados
            if (datos.tiempoMontajeMin() > locacion.getTiempoMontajeMin()) {
                verificarMontaje(locacion, datos.tiempoMontajeMin());
            }

            locacion.modificar(datos.nombre().trim(), datos.direccion().trim(), datos.tipo(), datos.capacidad(),
                    datos.distanciaKm(), datos.tiempoTrasladoMin(), datos.tiempoMontajeMin(),
                    datos.vehiculosRequeridos(), datos.requiereCamaraFrio(), limpiar(datos.otrosRequerimientos()));
            locaciones.actualizar(locacion);
            return locacion;
        });
    }

    /**
     * Inhabilitar no afecta lo ya planificado: solo impide usarla en
     * solicitudes y eventos nuevos. Devuelve cuántas reservas activas tiene.
     */
    public int cambiarHabilitacion(Usuario actor, int idLocacion, boolean habilitada) {
        return tx.ejecutar((Supplier<Integer>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            Locacion locacion = obtenerLocacion(idLocacion);
            if (habilitada) {
                locacion.habilitar();
            } else {
                locacion.inhabilitar();
            }
            locaciones.actualizar(locacion);
            return ocupacionesActivas(locacion).size();
        });
    }

    public void eliminarLocacion(Usuario actor, int idLocacion) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            Locacion locacion = obtenerLocacion(idLocacion);
            boolean enUso = solicitudes.listarTodos().stream()
                    .anyMatch(s -> s.getLocacion().getIdLocacion() == idLocacion)
                    || eventos.listarTodos().stream().anyMatch(e -> e.getLocacion().getIdLocacion() == idLocacion);
            if (enUso) {
                throw new EventoException("No se puede eliminar " + locacion.getNombre()
                        + ": la usan solicitudes o eventos. Podés inhabilitarla para que no se use en adelante");
            }
            locaciones.eliminar(idLocacion);
        });
    }

    /** Bloquea un rango de fechas (mantenimiento, uso propio, etc.). */
    public void bloquearFechas(Usuario actor, int idLocacion, LocalDate desde, LocalDate hasta, String motivo) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            Locacion locacion = obtenerLocacion(idLocacion);
            if (desde == null || hasta == null) {
                throw new DatosEventoInvalidosException("Debe indicar las fechas");
            }
            if (hasta.isBefore(desde)) {
                throw new DatosEventoInvalidosException("La fecha final no puede ser anterior a la inicial");
            }
            if (desde.isBefore(LocalDate.now())) {
                throw new DatosEventoInvalidosException("No se pueden bloquear fechas pasadas");
            }
            String limpio = limpiar(motivo);
            if (limpio.isEmpty()) {
                throw new DatosEventoInvalidosException("Debe indicar el motivo del bloqueo");
            }
            List<Ocupacion> ocupadas = reglas.ocupacionesEntre(locacion, desde, hasta);
            if (!ocupadas.isEmpty()) {
                throw new EventoException("No se pueden bloquear esas fechas porque hay reservas:\n  - "
                        + ocupadas.stream().map(Ocupacion::getDescripcion).collect(Collectors.joining("\n  - ")));
            }
            locacion.agregarBloqueo(new BloqueoFecha(desde, hasta, limpio));
            locaciones.actualizar(locacion);
        });
    }

    public void desbloquear(Usuario actor, int idLocacion, BloqueoFecha bloqueo) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_LOCACIONES);
            Locacion locacion = obtenerLocacion(idLocacion);
            locacion.quitarBloqueo(bloqueo);
            locaciones.actualizar(locacion);
        });
    }

    // =================================================================
    // Gestión de tipos de evento
    // =================================================================

    public List<TipoEvento> listarTipos(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_TIPOS_EVENTO);
        return tipos.listarTodos().stream()
                .sorted(Comparator.comparing(TipoEvento::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public TipoEvento crearTipo(Usuario actor, DatosTipoEvento datos) {
        return tx.ejecutar((Supplier<TipoEvento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_TIPOS_EVENTO);
            validarTipo(datos, null);
            TipoEvento tipo = new TipoEvento(datos.nombre().trim(), datos.duracionSugeridaHoras(),
                    datos.horaInicioSugerida(), limpiar(datos.requerimientosSugeridos()));
            tipos.guardar(tipo);
            return tipo;
        });
    }

    /** Los cambios solo afectan a los eventos que se creen después. */
    public TipoEvento modificarTipo(Usuario actor, int idTipo, DatosTipoEvento datos) {
        return tx.ejecutar((Supplier<TipoEvento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_TIPOS_EVENTO);
            TipoEvento tipo = obtenerTipo(idTipo);
            validarTipo(datos, tipo);
            tipo.modificar(datos.nombre().trim(), datos.duracionSugeridaHoras(), datos.horaInicioSugerida(),
                    limpiar(datos.requerimientosSugeridos()));
            tipos.actualizar(tipo);
            return tipo;
        });
    }

    public void cambiarActivacion(Usuario actor, int idTipo, boolean activo) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_TIPOS_EVENTO);
            TipoEvento tipo = obtenerTipo(idTipo);
            if (activo) {
                tipo.activar();
            } else {
                boolean quedaOtro = tipos.listarTodos().stream()
                        .anyMatch(t -> t.isActivo() && t.getIdTipoEvento() != idTipo);
                if (!quedaOtro) {
                    throw new EventoException("Tiene que quedar al menos un tipo de evento activo");
                }
                tipo.desactivar();
            }
            tipos.actualizar(tipo);
        });
    }

    public void eliminarTipo(Usuario actor, int idTipo) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_TIPOS_EVENTO);
            TipoEvento tipo = obtenerTipo(idTipo);
            boolean enUso = solicitudes.listarTodos().stream()
                    .anyMatch(s -> s.getTipoEvento().getIdTipoEvento() == idTipo)
                    || eventos.listarTodos().stream().anyMatch(e -> e.getTipo().getIdTipoEvento() == idTipo);
            if (enUso) {
                throw new EventoException("No se puede eliminar el tipo " + tipo.getNombre()
                        + ": lo usan solicitudes o eventos. Podés desactivarlo para que no se use en adelante");
            }
            boolean quedaOtro = tipos.listarTodos().stream()
                    .anyMatch(t -> t.isActivo() && t.getIdTipoEvento() != idTipo);
            if (!quedaOtro) {
                throw new EventoException("Tiene que quedar al menos un tipo de evento activo");
            }
            tipos.eliminar(idTipo);
        });
    }

    // =================================================================
    // Validaciones
    // =================================================================

    private void validarLocacion(DatosLocacion d, Locacion actual) {
        String nombre = limpiar(d.nombre());
        if (nombre.isEmpty() || nombre.length() > MAX_NOMBRE) {
            throw new DatosEventoInvalidosException("El nombre es obligatorio (hasta " + MAX_NOMBRE + " caracteres)");
        }
        boolean repetido = locaciones.listarTodos().stream()
                .anyMatch(l -> l.getNombre().equalsIgnoreCase(nombre)
                        && (actual == null || l.getIdLocacion() != actual.getIdLocacion()));
        if (repetido) {
            throw new DatosEventoInvalidosException("Ya existe una locación con ese nombre");
        }
        if (limpiar(d.direccion()).isEmpty()) {
            throw new DatosEventoInvalidosException("La dirección es obligatoria");
        }
        if (d.tipo() == null) {
            throw new DatosEventoInvalidosException("Debe indicar el tipo de locación");
        }
        rango(d.capacidad(), 1, 100_000, "La capacidad");
        if (d.distanciaKm() == null || d.distanciaKm() < 0 || d.distanciaKm() > 10_000) {
            throw new DatosEventoInvalidosException("La distancia a la sede debe estar entre 0 y 10.000 km");
        }
        rango(d.tiempoTrasladoMin(), 0, 24 * 60, "El tiempo de traslado (minutos)");
        rango(d.tiempoMontajeMin(), 0, 12 * 60, "El tiempo de montaje (minutos)");
        rango(d.vehiculosRequeridos(), 0, 100, "La cantidad de vehículos");
        if (limpiar(d.otrosRequerimientos()).length() > MAX_TEXTO) {
            throw new DatosEventoInvalidosException("Los requerimientos no pueden superar los " + MAX_TEXTO + " caracteres");
        }
    }

    private void validarTipo(DatosTipoEvento d, TipoEvento actual) {
        String nombre = limpiar(d.nombre());
        if (nombre.isEmpty() || nombre.length() > 60) {
            throw new DatosEventoInvalidosException("El nombre es obligatorio (hasta 60 caracteres)");
        }
        boolean repetido = tipos.listarTodos().stream()
                .anyMatch(t -> t.getNombre().equalsIgnoreCase(nombre)
                        && (actual == null || t.getIdTipoEvento() != actual.getIdTipoEvento()));
        if (repetido) {
            throw new DatosEventoInvalidosException("Ya existe un tipo de evento con ese nombre");
        }
        rango(d.duracionSugeridaHoras(), 1, 24, "La duración sugerida (horas)");
        if (d.horaInicioSugerida() == null) {
            throw new DatosEventoInvalidosException("Debe indicar la hora de inicio sugerida");
        }
        if (limpiar(d.requerimientosSugeridos()).length() > MAX_TEXTO) {
            throw new DatosEventoInvalidosException("Los requerimientos no pueden superar los " + MAX_TEXTO + " caracteres");
        }
    }

    /** Verifica que con el nuevo montaje no choquen las reservas ya existentes. */
    private void verificarMontaje(Locacion locacion, int nuevoMontaje) {
        List<Ocupacion> recalculadas = new ArrayList<>();
        for (Ocupacion o : ocupacionesActivas(locacion)) {
            Evento e = o.getEvento();
            recalculadas.add(e == null ? o : o.conIntervalo(new Intervalo(e.getInicio().minusMinutes(nuevoMontaje),
                    e.getFin().plusMinutes(nuevoMontaje))));
        }
        for (int i = 0; i < recalculadas.size(); i++) {
            for (int j = i + 1; j < recalculadas.size(); j++) {
                if (ReglasPlanificacion.chocan(recalculadas.get(i), recalculadas.get(j))) {
                    throw new EventoException("Con ese tiempo de montaje chocarían "
                            + recalculadas.get(i).getDescripcion().replaceAll(", más .*", ")") + " y "
                            + recalculadas.get(j).getDescripcion().replaceAll(", más .*", ")"));
                }
            }
        }
    }

    private List<Ocupacion> ocupacionesActivas(Locacion locacion) {
        return reglas.ocupaciones(locacion, null, Collections.<Integer>emptySet(), Collections.<Ocupacion>emptyList());
    }

    private static void rango(Integer valor, int min, int max, String campo) {
        if (valor == null || valor < min || valor > max) {
            throw new DatosEventoInvalidosException(campo + " debe estar entre " + min + " y " + max);
        }
    }

    private static String limpiar(String s) {
        return s == null ? "" : s.trim();
    }

    private Locacion obtenerLocacion(int id) {
        return locaciones.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Locación no encontrada"));
    }

    private TipoEvento obtenerTipo(int id) {
        return tipos.buscarPorId(id).orElseThrow(() -> new NoEncontradoException("Tipo de evento no encontrado"));
    }
}
