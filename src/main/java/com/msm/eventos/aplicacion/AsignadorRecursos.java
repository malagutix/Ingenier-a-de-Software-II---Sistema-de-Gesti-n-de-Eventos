package com.msm.eventos.aplicacion;

import com.msm.eventos.aplicacion.puertos.Notificador;
import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.FranjaHoraria;
import com.msm.eventos.dominio.comun.NoEncontradoException;
import com.msm.eventos.dominio.comun.ReglaNegocioException;
import com.msm.eventos.dominio.evento.HojaServicio;
import com.msm.eventos.dominio.recurso.AsignacionRecurso;
import com.msm.eventos.dominio.recurso.DisponibilidadRecurso;
import com.msm.eventos.dominio.recurso.Personal;
import com.msm.eventos.dominio.recurso.RecursoAsignable;
import com.msm.eventos.dominio.recurso.TipoPersonal;
import com.msm.eventos.dominio.servicio.RequerimientoPersonal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AsignadorRecursos {
    private final Repositorio<DisponibilidadRecurso> disponibilidades;
    private final Repositorio<AsignacionRecurso> asignaciones;
    private final Repositorio<Personal> personal;
    private final Notificador notificador;

    public AsignadorRecursos(Repositorio<DisponibilidadRecurso> disponibilidades,
                             Repositorio<AsignacionRecurso> asignaciones,
                             Repositorio<Personal> personal,
                             Notificador notificador) {
        this.disponibilidades = disponibilidades;
        this.asignaciones = asignaciones;
        this.personal = personal;
        this.notificador = notificador;
    }

    public DisponibilidadRecurso registrarDisponibilidad(RecursoAsignable recurso, FranjaHoraria franja) {
        if (!recurso.estaActivo()) {
            throw new ReglaNegocioException(recurso.getDescripcion() + " está dado de baja");
        }
        boolean superpuesta = disponibilidadesDe(recurso).stream().anyMatch(d -> d.getFranja().seSolapaCon(franja));
        if (superpuesta) {
            throw new ReglaNegocioException("La disponibilidad se superpone con otra ya cargada");
        }
        return disponibilidades.guardar(new DisponibilidadRecurso(recurso, franja));
    }

    public boolean estaDisponible(RecursoAsignable recurso, FranjaHoraria franja) {
        return estaDisponible(recurso, franja, null);
    }

    public List<Personal> buscarPersonalDisponible(TipoPersonal tipo, FranjaHoraria franja) {
        return personal.listar(p -> p.getTipoPersonal() == tipo && estaDisponible(p, franja));
    }

    public synchronized AsignacionRecurso asignar(RecursoAsignable recurso, HojaServicio hoja) {
        FranjaHoraria franja = hoja.getFranja();
        if (!estaDisponible(recurso, franja)) {
            throw new ReglaNegocioException(recurso.getDescripcion() + " no está disponible en la franja solicitada");
        }
        AsignacionRecurso asignacion = asignaciones.guardar(new AsignacionRecurso(recurso, franja, hoja.getId()));
        hoja.agregarAsignacion(asignacion);
        notificador.notificarAsignacion(asignacion);
        return asignacion;
    }

    public synchronized List<AsignacionRecurso> asignarPersonalRequerido(HojaServicio hoja) {
        Map<TipoPersonal, List<Personal>> seleccion = new EnumMap<>(TipoPersonal.class);
        personalFaltante(hoja).forEach((tipo, cantidad) -> {
            List<Personal> candidatos = buscarPersonalDisponible(tipo, hoja.getFranja());
            if (candidatos.size() < cantidad) {
                throw new ReglaNegocioException(String.format(
                        "Personal insuficiente de tipo %s: se requieren %d y hay %d disponibles",
                        tipo, cantidad, candidatos.size()));
            }
            seleccion.put(tipo, candidatos.subList(0, cantidad));
        });
        return seleccion.values().stream().flatMap(List::stream).map(p -> asignar(p, hoja)).toList();
    }

    public Map<TipoPersonal, Integer> personalFaltante(HojaServicio hoja) {
        Map<TipoPersonal, Integer> requeridos = hoja.getServicios().stream()
                .flatMap(s -> s.getRequerimientosPersonal().stream())
                .collect(Collectors.groupingBy(RequerimientoPersonal::tipoPersonal,
                        () -> new EnumMap<>(TipoPersonal.class),
                        Collectors.summingInt(r -> r.cantidadPara(hoja.getCantidadInvitados()))));
        hoja.getAsignaciones().stream()
                .map(AsignacionRecurso::getRecurso)
                .filter(Personal.class::isInstance)
                .map(Personal.class::cast)
                .forEach(p -> requeridos.computeIfPresent(p.getTipoPersonal(), (t, n) -> n - 1));
        requeridos.values().removeIf(n -> n <= 0);
        return requeridos;
    }

    public synchronized AsignacionRecurso reprogramar(String idAsignacion, FranjaHoraria nuevaFranja) {
        AsignacionRecurso asignacion = obtener(idAsignacion);
        if (!estaDisponible(asignacion.getRecurso(), nuevaFranja, asignacion)) {
            throw new ReglaNegocioException(asignacion.getRecurso().getDescripcion() + " no está disponible en la nueva franja");
        }
        FranjaHoraria anterior = asignacion.getFranja();
        asignacion.reprogramar(nuevaFranja);
        asignaciones.guardar(asignacion);
        notificador.notificarReprogramacion(asignacion, anterior);
        return asignacion;
    }

    /**
     * Reprograma todas las asignaciones de la hoja. Las que no pueden moverse se cancelan
     * y se devuelven para que el organizador las reemplace.
     */
    public synchronized List<AsignacionRecurso> reprogramarHoja(HojaServicio hoja, FranjaHoraria nuevaFranja) {
        hoja.setFranja(nuevaFranja);
        List<AsignacionRecurso> canceladas = new ArrayList<>();
        for (AsignacionRecurso asignacion : hoja.getAsignaciones()) {
            if (estaDisponible(asignacion.getRecurso(), nuevaFranja, asignacion)) {
                reprogramar(asignacion.getId(), nuevaFranja);
            } else {
                cancelar(asignacion.getId(), hoja);
                canceladas.add(asignacion);
            }
        }
        return canceladas;
    }

    public synchronized void cancelar(String idAsignacion, HojaServicio hoja) {
        AsignacionRecurso asignacion = obtener(idAsignacion);
        asignaciones.eliminar(idAsignacion);
        hoja.quitarAsignacion(asignacion);
        notificador.notificarCancelacion(asignacion);
    }

    public AsignacionRecurso registrarAsistencia(String idAsignacion, boolean asistio) {
        AsignacionRecurso asignacion = obtener(idAsignacion);
        asignacion.registrarAsistencia(asistio);
        return asignaciones.guardar(asignacion);
    }

    public AsignacionRecurso obtener(String idAsignacion) {
        return asignaciones.buscarPorId(idAsignacion)
                .orElseThrow(() -> new NoEncontradoException("Asignación", idAsignacion));
    }

    private boolean estaDisponible(RecursoAsignable recurso, FranjaHoraria franja, AsignacionRecurso ignorada) {
        return recurso.estaActivo()
                && disponibilidadesDe(recurso).stream().anyMatch(d -> d.cubre(franja))
                && asignacionesDe(recurso).stream()
                    .filter(a -> ignorada == null || !a.getId().equals(ignorada.getId()))
                    .noneMatch(a -> a.getFranja().seSolapaCon(franja));
    }

    private List<DisponibilidadRecurso> disponibilidadesDe(RecursoAsignable recurso) {
        return disponibilidades.listar(d -> d.getRecurso().getId().equals(recurso.getId()));
    }

    private List<AsignacionRecurso> asignacionesDe(RecursoAsignable recurso) {
        return asignaciones.listar(a -> a.getRecurso().getId().equals(recurso.getId()));
    }
}
