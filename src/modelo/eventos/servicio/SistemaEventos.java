package modelo.eventos.servicio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import modelo.eventos.entidades.DatosEvento;
import modelo.eventos.entidades.DatosSolicitud;
import modelo.eventos.entidades.EstadoSolicitud;
import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.EventoFactory;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.HojaServicio;
import modelo.eventos.entidades.Intervalo;
import modelo.eventos.entidades.Locacion;
import modelo.eventos.entidades.Planificacion;
import modelo.eventos.entidades.SolicitudEvento;
import modelo.eventos.entidades.TipoEvento;
import modelo.eventos.entidades.estado.TransicionEvento;
import modelo.eventos.excepciones.DatosEventoInvalidosException;
import modelo.eventos.excepciones.EventoException;
import modelo.eventos.excepciones.NoEncontradoException;
import modelo.eventos.repositorio.EventoRepository;
import modelo.eventos.repositorio.HojaServicioRepository;
import modelo.eventos.repositorio.HojaServicioRepositoryEnMemoria;
import modelo.eventos.repositorio.LocacionRepository;
import modelo.eventos.repositorio.PlanificacionRepository;
import modelo.eventos.repositorio.SolicitudRepository;
import modelo.eventos.repositorio.TipoEventoRepository;
import modelo.eventos.servicio.ReglasPlanificacion.Ocupacion;
import modelo.persistencia.UnidadDeTrabajo;
import modelo.usuarios.entidades.Permiso;
import modelo.usuarios.entidades.Usuario;
import modelo.usuarios.repositorio.UsuarioRepository;
import modelo.usuarios.servicio.GestorUsuarios;

/**
 * Patrón Facade: "ventanilla única" para solicitudes, planificaciones,
 * eventos, hojas de servicio y calendario. Las pantallas solo conocen esta
 * clase (y GestorCatalogos para locaciones y tipos de evento).
 *
 * Casos de uso cubiertos: CDU 008 a CDU 015.
 */
public class SistemaEventos {

    private static final int MAX_DURACION_HORAS = 24;
    private static final int MAX_LARGO_REQUERIMIENTOS = 500;
    private static final int MAX_DIAS_POR_PLANIFICACION = 30;
    private static final int MAX_LARGO_NOMBRE = 80;

    private final ControlAcceso acceso;
    private final UsuarioRepository usuarios;
    private final SolicitudRepository solicitudes;
    private final PlanificacionRepository planificaciones;
    private final EventoRepository eventos;
    private final LocacionRepository locaciones;
    private final TipoEventoRepository tipos;
    private final EventoFactory eventoFactory;
    private final ReglasPlanificacion reglas;
    private final HojaServicioRepository hojas;
    private final UnidadDeTrabajo tx;

    /** Versión en memoria (sin base de datos). */
    public SistemaEventos(GestorUsuarios gestorUsuarios, UsuarioRepository usuarios,
                          SolicitudRepository solicitudes, PlanificacionRepository planificaciones,
                          EventoRepository eventos, LocacionRepository locaciones, TipoEventoRepository tipos,
                          EventoFactory eventoFactory, ReglasPlanificacion reglas) {
        this(gestorUsuarios, usuarios, solicitudes, planificaciones, eventos, locaciones, tipos, eventoFactory,
                reglas, new HojaServicioRepositoryEnMemoria(), UnidadDeTrabajo.SIN_TRANSACCION);
    }

    public SistemaEventos(GestorUsuarios gestorUsuarios, UsuarioRepository usuarios,
                          SolicitudRepository solicitudes, PlanificacionRepository planificaciones,
                          EventoRepository eventos, LocacionRepository locaciones, TipoEventoRepository tipos,
                          EventoFactory eventoFactory, ReglasPlanificacion reglas, HojaServicioRepository hojas,
                          UnidadDeTrabajo tx) {
        this.hojas = hojas;
        this.tx = tx;
        this.acceso = new ControlAcceso(gestorUsuarios);
        this.usuarios = usuarios;
        this.solicitudes = solicitudes;
        this.planificaciones = planificaciones;
        this.eventos = eventos;
        this.locaciones = locaciones;
        this.tipos = tipos;
        this.eventoFactory = eventoFactory;
        this.reglas = reglas;
    }

    // =================================================================
    // Consultas auxiliares
    // =================================================================

    /** Todas las locaciones (para mostrar las que ya usa un evento). */
    public List<Locacion> listarLocaciones() {
        return locaciones.listarTodos();
    }

    /** Locaciones habilitadas, para elegir en solicitudes y eventos nuevos. */
    public List<Locacion> listarLocacionesHabilitadas() {
        return locaciones.listarTodos().stream().filter(Locacion::isHabilitada).collect(Collectors.toList());
    }

    /** Tipos de evento activos, para elegir en solicitudes y eventos. */
    public List<TipoEvento> listarTiposActivos() {
        return tipos.listarTodos().stream().filter(TipoEvento::isActivo).collect(Collectors.toList());
    }

    public List<Usuario> listarClientes(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
        return usuarios.listarTodos().stream()
                .filter(u -> "CLIENTE".equals(u.getRol().getNombre()))
                .collect(Collectors.toList());
    }

    public Optional<Planificacion> planificacionDeSolicitud(int idSolicitud) {
        return planificaciones.buscarPorSolicitud(idSolicitud);
    }

    // =================================================================
    // CDU 008 - Crear solicitud (RF008)
    // =================================================================

    public SolicitudEvento crearSolicitud(Usuario actor, DatosSolicitud datos) {
        return tx.ejecutar((Supplier<SolicitudEvento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
            Usuario cliente = validarSolicitud(datos);
            TipoEvento tipo = obtenerTipoActivo(datos.idTipoEvento(), null);
            Locacion locacion = obtenerLocacion(datos.idLocacion());
            verificarDiaCompleto(locacion, datos.fechaSolicitada(), datos.cantidadInvitados(), null);

            SolicitudEvento solicitud = new SolicitudEvento(cliente, datos.fechaSolicitada(), tipo,
                    datos.cantidadInvitados(), locacion, datos.menu());
            solicitudes.guardar(solicitud); // el repositorio asigna el ID único
            return solicitud;
        });
    }

    public List<SolicitudEvento> listarSolicitudes(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
        return solicitudes.listarTodos().stream()
                .filter(s -> !s.estaEliminada())
                .sorted(Comparator.comparing(SolicitudEvento::getFechaSolicitada))
                .collect(Collectors.toList());
    }

    // =================================================================
    // CDU 009 - Modificar solicitud (RF009)
    // =================================================================

    public SolicitudEvento modificarSolicitud(Usuario actor, int idSolicitud, DatosSolicitud datos) {
        return tx.ejecutar((Supplier<SolicitudEvento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
            SolicitudEvento solicitud = obtenerSolicitud(idSolicitud);
            if (planificaciones.buscarPorSolicitud(idSolicitud).isPresent()) {
                throw new EventoException("La solicitud ya tiene una planificación con eventos. Modificá los eventos");
            }
            if (!solicitud.estaVigente()) {
                throw new EventoException("No se puede modificar una solicitud rechazada");
            }
            Usuario cliente = validarSolicitud(datos);
            TipoEvento tipo = obtenerTipoActivo(datos.idTipoEvento(), solicitud.getTipoEvento());
            Locacion locacion = obtenerLocacion(datos.idLocacion());
            verificarDiaCompleto(locacion, datos.fechaSolicitada(), datos.cantidadInvitados(), idSolicitud);

            solicitud.modificar(cliente, datos.fechaSolicitada(), tipo, datos.cantidadInvitados(),
                    locacion, datos.menu());
            solicitudes.actualizar(solicitud);
            return solicitud;
        });
    }

    /**
     * Acepta una solicitud pendiente y crea su evento (PENDIENTE DE RESERVA) en
     * la fecha solicitada, con la hora y duración sugeridas del tipo. Después
     * se le pueden agregar más días desde Planificaciones.
     *
     * @return el evento creado
     */
    public Evento aceptarSolicitud(Usuario actor, int idSolicitud) {
        return aceptarSolicitud(actor, idSolicitud, null);
    }

    /** @param nombrePlanificacion nombre de la planificación, o null para generarlo */
    public Evento aceptarSolicitud(Usuario actor, int idSolicitud, String nombrePlanificacion) {
        return tx.ejecutar((Supplier<Evento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
            SolicitudEvento solicitud = obtenerSolicitud(idSolicitud);
            if (!solicitud.estaPendiente()) {
                throw new EventoException("Solo se puede aceptar una solicitud pendiente (esta está "
                        + solicitud.getEstado() + ")");
            }
            solicitud.aceptar();
            try {
                solicitudes.actualizar(solicitud);
                return planificarEventos(actor, idSolicitud, solicitud.getFechaSolicitada(), null, null, 1,
                        nombrePlanificacion).get(0);
            } catch (RuntimeException e) {
                solicitud.restaurarEstado(EstadoSolicitud.PENDIENTE); // en memoria no hay rollback
                throw e;
            }
        });
    }

    public void rechazarSolicitud(Usuario actor, int idSolicitud) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
            SolicitudEvento solicitud = obtenerSolicitud(idSolicitud);
            if (!solicitud.estaVigente()) {
                throw new EventoException("La solicitud ya está rechazada");
            }
            if (planificaciones.buscarPorSolicitud(idSolicitud).isPresent()) {
                throw new EventoException("No se puede rechazar: la solicitud ya tiene eventos planificados");
            }
            solicitud.rechazar();
            solicitudes.actualizar(solicitud);
        });
    }

    // =================================================================
    // CDU 010 - Eliminar solicitud (RF010)
    // =================================================================

    /** Elimina la solicitud junto con sus eventos, si tiene. */
    public void eliminarSolicitud(Usuario actor, int idSolicitud) {
        eliminarSolicitud(actor, idSolicitud, true);
    }

    /**
     * @param eliminarEventos si la solicitud tiene eventos: true los elimina junto
     *                        con ella; false la quita de la lista (ELIMINADA) y
     *                        conserva sus eventos, que la siguen necesitando
     */
    public void eliminarSolicitud(Usuario actor, int idSolicitud, boolean eliminarEventos) {
        eliminarSolicitudes(actor, Collections.singletonList(idSolicitud), eliminarEventos);
    }

    /**
     * CDU 010: elimina una o más solicitudes. Es todo o nada: primero se
     * verifican todas (paso 2: si son parte de una planificación, sus eventos
     * tienen que poder eliminarse) y recién después se borra.
     *
     * @param eliminarEventos para las que tienen eventos: true los elimina junto
     *                        con ellas; false las quita de la lista (ELIMINADA)
     *                        y conserva sus eventos, que las siguen necesitando
     */
    public void eliminarSolicitudes(Usuario actor, List<Integer> idsSolicitudes, boolean eliminarEventos) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_SOLICITUDES);
            if (idsSolicitudes.isEmpty()) {
                throw new EventoException("Seleccioná al menos una solicitud");
            }
            // 1) Verificar todas antes de borrar nada
            List<SolicitudEvento> seleccionadas = new ArrayList<>();
            for (int id : new LinkedHashSet<>(idsSolicitudes)) {
                SolicitudEvento solicitud = obtenerSolicitud(id);
                if (eliminarEventos) {
                    for (Evento evento : eventosDeSolicitud(id)) {
                        try {
                            evento.verificarEliminable();
                        } catch (EventoException e) {
                            throw new EventoException("No se puede eliminar la solicitud #" + id + ": "
                                    + e.getMessage() + " (" + evento.descripcionCorta() + ")");
                        }
                    }
                }
                seleccionadas.add(solicitud);
            }

            // 2) Eliminar
            for (SolicitudEvento solicitud : seleccionadas) {
                int id = solicitud.getIdSolicitud();
                Optional<Planificacion> planificacion = planificaciones.buscarPorSolicitud(id);
                List<Evento> suyos = eventosDeSolicitud(id);
                if (!suyos.isEmpty() && !eliminarEventos) {
                    solicitud.marcarEliminada();
                    solicitudes.actualizar(solicitud);
                    continue;
                }
                for (Evento evento : suyos) {
                    evento.notificar(evento.descripcionCorta() + " fue eliminado junto con su solicitud");
                    eventos.eliminar(evento.getIdEvento());
                }
                planificacion.ifPresent(p -> planificaciones.eliminar(p.getIdPlanificacion()));
                solicitudes.eliminar(id);
            }
        });
    }

    private List<Evento> eventosDeSolicitud(int idSolicitud) {
        return planificaciones.buscarPorSolicitud(idSolicitud)
                .map(p -> (List<Evento>) new ArrayList<>(p.getEventos()))
                .orElseGet(ArrayList::new);
    }

    // =================================================================
    // CDU 011 - Crear evento (RF011) + Planificación de múltiples eventos
    // =================================================================

    /** Solicitudes aceptadas a partir de las cuales se pueden planificar eventos. */
    public List<SolicitudEvento> listarSolicitudesParaPlanificar(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
        return solicitudes.listarTodos().stream()
                .filter(SolicitudEvento::estaAceptada)
                .sorted(Comparator.comparing(SolicitudEvento::getFechaSolicitada))
                .collect(Collectors.toList());
    }

    /** Fecha sugerida para el próximo evento: la de la solicitud o el día siguiente al último evento. */
    public LocalDate fechaSugerida(SolicitudEvento solicitud) {
        return planificaciones.buscarPorSolicitud(solicitud.getIdSolicitud())
                .filter(p -> !p.estaVacia())
                .map(p -> p.getFechaFin().plusDays(1))
                .orElse(solicitud.getFechaSolicitada());
    }

    /**
     * Crea uno o varios eventos en días consecutivos, a la misma hora, a partir
     * de una solicitud. Si la solicitud todavía no tiene planificación, la crea
     * con el nombre indicado; si ya tiene, agrega los eventos a la existente.
     * Es todo o nada: si una fecha no está disponible, no se crea ningún evento.
     */
    public List<Evento> planificarEventos(Usuario organizador, int idSolicitud, LocalDate fechaInicio,
                                          LocalTime horaInicio, Integer duracionHoras, int cantidadDias,
                                          String nombrePlanificacion) {
        return tx.ejecutar((Supplier<List<Evento>>) () -> {
            acceso.verificar(organizador, Permiso.GESTIONAR_EVENTOS);
            SolicitudEvento solicitud = obtenerSolicitud(idSolicitud);
            if (!solicitud.estaAceptada()) {
                throw new EventoException("No se pudo crear el evento: la solicitud está "
                        + solicitud.getEstado() + ". Primero tiene que aceptarse");
            }
            if (cantidadDias < 1 || cantidadDias > MAX_DIAS_POR_PLANIFICACION) {
                throw new DatosEventoInvalidosException("La cantidad de días debe estar entre 1 y " + MAX_DIAS_POR_PLANIFICACION);
            }
            TipoEvento tipo = solicitud.getTipoEvento();
            LocalTime hora = horaInicio != null ? horaInicio : tipo.getHoraInicioSugerida();
            int duracion = duracionHoras != null ? duracionHoras : tipo.getDuracionSugerida();
            validarDuracion(duracion);
            Optional<Planificacion> existente = planificaciones.buscarPorSolicitud(idSolicitud);
            String nombre = existente.isPresent() ? null : validarNombre(nombrePlanificacion, solicitud);

            // 1) Validar todas las fechas antes de crear nada (incluye superposición entre ellas)
            Locacion locacion = solicitud.getLocacion();
            List<LocalDate> fechas = new ArrayList<>();
            List<Ocupacion> pendientes = new ArrayList<>();
            for (int i = 0; i < cantidadDias; i++) {
                LocalDate fecha = fechaInicio == null ? null : fechaInicio.plusDays(i);
                verificarFechaFutura(fecha);
                verificarEvento(locacion, fecha, hora, duracion, solicitud.getCantidadInvitados(), idSolicitud,
                        Collections.<Integer>emptySet(), pendientes);
                pendientes.add(ReglasPlanificacion.reservaEvento(locacion, fecha, hora, duracion, 0,
                        "otro día de esta misma planificación (" + Formato.fecha(fecha) + ")"));
                fechas.add(fecha);
            }

            // 2) Crear u obtener la planificación y generar los eventos (Factory Method)
            Planificacion planificacion = existente.orElseGet(() -> {
                Planificacion nueva = new Planificacion(nombre, solicitud);
                planificaciones.guardar(nueva);
                return nueva;
            });
            List<Evento> creados = new ArrayList<>();
            for (LocalDate fecha : fechas) {
                Evento evento = eventoFactory.crearEvento(planificacion, fecha, hora, duracion, organizador);
                suscribirObservadores(evento, organizador, solicitud.getCliente());
                eventos.guardar(evento); // queda registrado en el calendario
                planificacion.agregarEvento(evento);
                evento.notificar(evento.descripcionCorta() + " fue creado en la planificación \""
                        + planificacion.getNombre() + "\"");
                creados.add(evento);
            }
            planificaciones.actualizar(planificacion);
            return creados;
        });
    }

    public List<Planificacion> listarPlanificaciones(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
        return planificaciones.listarTodos().stream()
                .sorted(Comparator.comparing(Planificacion::getFechaInicio,
                        Comparator.nullsLast(Comparator.<LocalDate>naturalOrder())))
                .collect(Collectors.toList());
    }

    public void renombrarPlanificacion(Usuario actor, int idPlanificacion, String nuevoNombre) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
            Planificacion planificacion = obtenerPlanificacion(idPlanificacion);
            planificacion.renombrar(validarNombre(nuevoNombre, planificacion.getSolicitud()));
            planificaciones.actualizar(planificacion);
        });
    }

    /**
     * Regla de reprogramación en bloque: mueve todos los eventos activos de la
     * planificación la misma cantidad de días, manteniendo horarios y distancia
     * entre ellos. Cada evento pasa a REPROGRAMADO mediante el patrón State.
     */
    public void reprogramarPlanificacion(Usuario actor, int idPlanificacion, LocalDate nuevaFechaInicio) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.VALIDAR_EVENTO);
            Planificacion planificacion = obtenerPlanificacion(idPlanificacion);
            List<Evento> activos = planificacion.getEventosActivos();
            if (activos.isEmpty()) {
                throw new EventoException("La planificación no tiene eventos activos para reprogramar");
            }
            for (Evento e : activos) {
                if (!e.getEstado().transicionesPosibles().contains(TransicionEvento.REPROGRAMAR)) {
                    throw new EventoException("No se puede reprogramar la planificación: el evento #" + e.getIdEvento()
                            + " está " + e.getEstado().getNombre());
                }
            }
            verificarFechaFutura(nuevaFechaInicio);
            long desplazamiento = ChronoUnit.DAYS.between(planificacion.getFechaInicioActiva(), nuevaFechaInicio);
            if (desplazamiento == 0) {
                throw new DatosEventoInvalidosException("La nueva fecha de inicio es igual a la actual");
            }
            Set<Integer> idsPlanificacion = idsDe(planificacion);
            for (Evento e : activos) {
                LocalDate nueva = e.getFecha().plusDays(desplazamiento);
                verificarFechaFutura(nueva);
                verificarEvento(e.getLocacion(), nueva, e.getHoraInicio(), e.getDuracionHoras(),
                        e.getCantidadAsistentes(), planificacion.getSolicitud().getIdSolicitud(), idsPlanificacion,
                        Collections.<Ocupacion>emptyList());
            }
            for (Evento e : activos) {
                e.aplicar(TransicionEvento.REPROGRAMAR, e.getFecha().plusDays(desplazamiento));
                eventos.actualizar(e);
            }
            planificaciones.actualizar(planificacion);
        });
    }

    /** Cancela en bloque todos los eventos activos de la planificación. */
    public void cancelarPlanificacion(Usuario actor, int idPlanificacion) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.VALIDAR_EVENTO);
            Planificacion planificacion = obtenerPlanificacion(idPlanificacion);
            List<Evento> activos = planificacion.getEventosActivos();
            if (activos.isEmpty()) {
                throw new EventoException("La planificación no tiene eventos activos para cancelar");
            }
            for (Evento e : activos) {
                if (!e.getEstado().transicionesPosibles().contains(TransicionEvento.CANCELAR)) {
                    throw new EventoException("No se puede cancelar la planificación: el evento #" + e.getIdEvento()
                            + " está " + e.getEstado().getNombre());
                }
            }
            for (Evento e : activos) {
                e.aplicar(TransicionEvento.CANCELAR, null);
                eventos.actualizar(e);
            }
            planificaciones.actualizar(planificacion);
        });
    }

    public List<Evento> listarEventos(Usuario actor) {
        acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
        return eventos.listarTodos().stream()
                .sorted(Comparator.comparing(Evento::getInicio))
                .collect(Collectors.toList());
    }

    // =================================================================
    // CDU 012 - Eliminar evento (RF012)
    // =================================================================

    public void eliminarEvento(Usuario actor, int idEvento) {
        eliminarEventos(actor, Collections.singletonList(idEvento));
    }

    /**
     * Elimina uno o más eventos. Es todo o nada: primero se verifica que todos
     * se puedan eliminar (ninguno EN PROGRESO) y recién después se borra.
     */
    public void eliminarEventos(Usuario actor, List<Integer> idsEventos) {
        tx.ejecutar((Runnable) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
            if (idsEventos.isEmpty()) {
                throw new EventoException("Seleccioná al menos un evento");
            }
            List<Evento> seleccionados = new ArrayList<>();
            for (int id : new LinkedHashSet<>(idsEventos)) {
                Evento evento = obtenerEvento(id);
                try {
                    evento.verificarEliminable();
                } catch (EventoException e) {
                    throw idsEventos.size() == 1 ? e
                            : new EventoException(e.getMessage() + " (" + evento.descripcionCorta() + ")");
                }
                seleccionados.add(evento);
            }
            for (Evento evento : seleccionados) {
                evento.notificar(evento.descripcionCorta() + " fue eliminado");
                eventos.eliminar(evento.getIdEvento());

                // La planificación se conserva aunque quede vacía: la solicitud ya fue
                // planificada y no vuelve a reservar su fecha original (que puede estar
                // tomada por otra solicitud). Se pueden volver a planificar eventos desde ella.
                Planificacion planificacion = evento.getPlanificacion();
                planificacion.quitarEvento(evento);
                planificaciones.actualizar(planificacion);
            }
        });
    }

    // =================================================================
    // CDU 013 - Validar evento (RF013): cambio de estado
    // =================================================================

    public Evento cambiarEstado(Usuario actor, int idEvento, TransicionEvento transicion, LocalDate nuevaFecha) {
        return tx.ejecutar((Supplier<Evento>) () -> {
            acceso.verificar(actor, Permiso.VALIDAR_EVENTO);
            Evento evento = obtenerEvento(idEvento);
            if (transicion == TransicionEvento.REPROGRAMAR) {
                verificarFechaFutura(nuevaFecha);
                if (nuevaFecha.equals(evento.getFecha())) {
                    throw new DatosEventoInvalidosException("La nueva fecha es igual a la actual");
                }
                verificarEvento(evento.getLocacion(), nuevaFecha, evento.getHoraInicio(), evento.getDuracionHoras(),
                        evento.getCantidadAsistentes(), evento.getSolicitud().getIdSolicitud(),
                        Collections.singleton(idEvento), Collections.<Ocupacion>emptyList());
            }
            evento.aplicar(transicion, nuevaFecha); // State: el estado decide si se puede
            eventos.actualizar(evento);
            return evento;
        });
    }

    // =================================================================
    // CDU 014 - Modificar evento (RF014)
    // =================================================================

    public Evento modificarEvento(Usuario actor, int idEvento, DatosEvento datos) {
        return tx.ejecutar((Supplier<Evento>) () -> {
            acceso.verificar(actor, Permiso.GESTIONAR_EVENTOS);
            Evento evento = obtenerEvento(idEvento);
            evento.getEstado().verificarModificable(); // "No se puede modificar un evento ya finalizado"

            TipoEvento tipo = obtenerTipoActivo(datos.idTipoEvento(), evento.getTipo());
            if (datos.horaInicio() == null) {
                throw new DatosEventoInvalidosException("Debe indicar la hora de inicio");
            }
            if (datos.duracionHoras() == null) {
                throw new DatosEventoInvalidosException("Debe indicar la duración");
            }
            validarDuracion(datos.duracionHoras());
            if (datos.cantidadAsistentes() == null || datos.cantidadAsistentes() < 1) {
                throw new DatosEventoInvalidosException("La cantidad de asistentes debe ser mayor a cero");
            }
            String requerimientos = datos.requerimientosEspecificos() == null ? "" : datos.requerimientosEspecificos().trim();
            if (requerimientos.length() > MAX_LARGO_REQUERIMIENTOS) {
                throw new DatosEventoInvalidosException("Los requerimientos no pueden superar los "
                        + MAX_LARGO_REQUERIMIENTOS + " caracteres");
            }
            Locacion locacion = obtenerLocacion(datos.idLocacion());
            if (!locacion.isHabilitada() && locacion.getIdLocacion() != evento.getLocacion().getIdLocacion()) {
                throw new DatosEventoInvalidosException("La locación " + locacion.getNombre() + " no está habilitada");
            }
            verificarEventoModificado(evento, locacion, datos.horaInicio(), datos.duracionHoras(),
                    datos.cantidadAsistentes());

            evento.modificar(tipo, datos.horaInicio(), datos.duracionHoras(), datos.cantidadAsistentes(), locacion,
                    requerimientos);
            eventos.actualizar(evento);
            return evento;
        });
    }

    // =================================================================
    // CDU 015 - Generar hoja de servicio (RF017)
    // =================================================================

    public HojaServicio generarHojaServicio(Usuario actor, int idEvento) {
        return tx.ejecutar((Supplier<HojaServicio>) () -> {
            acceso.verificar(actor, Permiso.GENERAR_HOJA_SERVICIO);
            Evento evento = obtenerEvento(idEvento);
            HojaServicio hoja = evento.generarHojaServicio();
            hojas.guardar(hoja); // queda registrada con su ID
            return hoja;
        });
    }

    // =================================================================
    // Calendario (VisualizarCalendario del diagrama de clases)
    // =================================================================

    /** Eventos del mes. Quien gestiona eventos ve todos; un cliente ve solo los suyos. */
    public List<Evento> eventosDelMes(Usuario actor, YearMonth mes) {
        return eventosVisibles(actor)
                .filter(e -> YearMonth.from(e.getFecha()).equals(mes))
                .sorted(Comparator.comparing(Evento::getInicio))
                .collect(Collectors.toList());
    }

    /** Próximo evento (desde hoy) que el usuario puede ver en el calendario. */
    public Optional<Evento> proximoEvento(Usuario actor) {
        LocalDate hoy = LocalDate.now();
        return eventosVisibles(actor)
                .filter(e -> !e.getFecha().isBefore(hoy))
                .min(Comparator.comparing(Evento::getInicio));
    }

    private Stream<Evento> eventosVisibles(Usuario actor) {
        acceso.verificar(actor, Permiso.VER_CALENDARIO);
        boolean veTodos = actor.tienePermiso(Permiso.GESTIONAR_EVENTOS);
        return eventos.listarTodos().stream()
                .filter(e -> veTodos || e.getCliente().getIdUsuario() == actor.getIdUsuario());
    }

    // =================================================================
    // Validaciones
    // =================================================================

    private Usuario validarSolicitud(DatosSolicitud datos) {
        if (datos.idCliente() == null) {
            throw new DatosEventoInvalidosException("Debe seleccionar un cliente");
        }
        Usuario cliente = usuarios.buscarPorId(datos.idCliente())
                .filter(u -> "CLIENTE".equals(u.getRol().getNombre()))
                .orElseThrow(() -> new DatosEventoInvalidosException("El cliente seleccionado no es válido"));
        verificarFechaFutura(datos.fechaSolicitada());
        if (datos.cantidadInvitados() == null || datos.cantidadInvitados() < 1) {
            throw new DatosEventoInvalidosException("La cantidad de invitados debe ser mayor a cero");
        }
        if (datos.menu() == null) {
            throw new DatosEventoInvalidosException("Debe seleccionar un menú");
        }
        return cliente;
    }

    private void verificarFechaFutura(LocalDate fecha) {
        if (fecha == null) {
            throw new DatosEventoInvalidosException("Debe indicar la fecha");
        }
        if (!fecha.isAfter(LocalDate.now())) {
            throw new DatosEventoInvalidosException("La fecha debe ser posterior a la fecha actual");
        }
    }

    private void validarDuracion(int duracion) {
        if (duracion < 1 || duracion > MAX_DURACION_HORAS) {
            throw new DatosEventoInvalidosException("La duración debe estar entre 1 y " + MAX_DURACION_HORAS + " horas");
        }
    }

    /** Una solicitud sin planificar reserva su día. */
    private void verificarDiaCompleto(Locacion locacion, LocalDate fecha, int personas, Integer excluirSolicitud) {
        reglas.verificar(locacion, ReglasPlanificacion.reservaDia(locacion, fecha, personas, "la solicitud"),
                Collections.singletonList(fecha), excluirSolicitud, Collections.<Integer>emptySet(),
                Collections.<Ocupacion>emptyList());
    }

    /** Un evento ocupa su horario más el montaje y desmontaje de la locación. */
    private void verificarEvento(Locacion locacion, LocalDate fecha, LocalTime hora, int duracion, int personas,
                                 Integer excluirSolicitud, Collection<Integer> excluirEventos,
                                 List<Ocupacion> pendientes) {
        Ocupacion reserva = ReglasPlanificacion.reservaEvento(locacion, fecha, hora, duracion, personas, "el evento");
        Intervalo evento = new Intervalo(fecha.atTime(hora), fecha.atTime(hora).plusHours(duracion));
        reglas.verificar(locacion, reserva, ReglasPlanificacion.dias(evento), excluirSolicitud, excluirEventos,
                pendientes);
    }

    /**
     * Al modificar un evento: si cambia la locación o el horario se verifican
     * todas las reglas. Si no cambian, solo se controla la capacidad (así un
     * bloqueo o una inhabilitación cargados después no impiden corregir, por
     * ejemplo, los requerimientos).
     */
    private void verificarEventoModificado(Evento evento, Locacion locacion, LocalTime hora, int duracion,
                                           int personas) {
        boolean mismaLocacion = locacion.getIdLocacion() == evento.getLocacion().getIdLocacion();
        boolean mismoHorario = hora.equals(evento.getHoraInicio()) && duracion == evento.getDuracionHoras();
        if (!mismaLocacion || !mismoHorario) {
            verificarEvento(locacion, evento.getFecha(), hora, duracion, personas,
                    evento.getSolicitud().getIdSolicitud(), Collections.singleton(evento.getIdEvento()),
                    Collections.<Ocupacion>emptyList());
        } else if (personas > locacion.getCapacidad()) {
            throw new DatosEventoInvalidosException("La locación " + locacion.getNombre()
                    + " tiene capacidad para " + locacion.getCapacidad() + " personas");
        }
    }

    private String validarNombre(String nombre, SolicitudEvento solicitud) {
        String limpio = nombre == null ? "" : nombre.trim();
        if (limpio.isEmpty()) {
            limpio = solicitud.getTipoEvento().getNombre() + " - " + solicitud.getCliente().getNombre() + " "
                    + solicitud.getCliente().getApellido();
        }
        if (limpio.length() > MAX_LARGO_NOMBRE) {
            throw new DatosEventoInvalidosException("El nombre no puede superar los " + MAX_LARGO_NOMBRE + " caracteres");
        }
        return limpio;
    }

    /** Observer: se suscriben el organizador, el cliente y los administradores. */
    private void suscribirObservadores(Evento evento, Usuario organizador, Usuario cliente) {
        suscribirObservadores(evento, organizador, cliente, usuarios.listarTodos());
    }

    /**
     * Regla de suscripción, también usada al leer los eventos de la base de
     * datos (los observadores no se guardan: se vuelven a suscribir).
     */
    public static void suscribirObservadores(Evento evento, Usuario organizador, Usuario cliente,
                                             List<Usuario> todos) {
        evento.agregarObserver(organizador);
        evento.agregarObserver(cliente);
        for (Usuario u : todos) {
            if (u.tienePermiso(Permiso.ASIGNAR_ROL)) {
                evento.agregarObserver(u);
            }
        }
    }

    private static Set<Integer> idsDe(Planificacion planificacion) {
        Set<Integer> ids = new HashSet<>();
        for (Evento e : planificacion.getEventos()) {
            ids.add(e.getIdEvento());
        }
        return ids;
    }

    private Planificacion obtenerPlanificacion(int id) {
        return planificaciones.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Planificación no encontrada"));
    }

    private SolicitudEvento obtenerSolicitud(int id) {
        return solicitudes.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Solicitud no encontrada"));
    }

    private Evento obtenerEvento(int id) {
        return eventos.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Error: El evento no existe"));
    }

    private Locacion obtenerLocacion(Integer id) {
        if (id == null) {
            throw new DatosEventoInvalidosException("Debe seleccionar una locación");
        }
        return locaciones.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Locación no encontrada"));
    }

    /**
     * Devuelve el tipo pedido. Debe estar activo, salvo que sea el que ya tenía
     * (un tipo desactivado sigue sirviendo para lo que ya se cargó).
     */
    private TipoEvento obtenerTipoActivo(Integer id, TipoEvento actual) {
        if (id == null) {
            throw new DatosEventoInvalidosException("Debe indicar el tipo de evento");
        }
        TipoEvento tipo = tipos.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoException("Tipo de evento no encontrado"));
        if (!tipo.isActivo() && (actual == null || actual.getIdTipoEvento() != tipo.getIdTipoEvento())) {
            throw new DatosEventoInvalidosException("El tipo de evento " + tipo.getNombre() + " está desactivado");
        }
        return tipo;
    }
}
