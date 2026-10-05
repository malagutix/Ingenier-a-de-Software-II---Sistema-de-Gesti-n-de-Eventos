package com.msm.eventos.aplicacion;

import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.NoEncontradoException;
import com.msm.eventos.dominio.recurso.DisponibilidadRecurso;
import com.msm.eventos.dominio.recurso.Personal;
import com.msm.eventos.dominio.recurso.TipoPersonal;

import java.util.List;
import java.util.function.Consumer;

public class GestorPersonal {
    private final Repositorio<Personal> personal;
    private final Repositorio<DisponibilidadRecurso> disponibilidades;

    public GestorPersonal(Repositorio<Personal> personal, Repositorio<DisponibilidadRecurso> disponibilidades) {
        this.personal = personal;
        this.disponibilidades = disponibilidades;
    }

    public Personal registrar(Personal nuevo) {
        return personal.guardar(nuevo);
    }

    public Personal editar(String codigo, Consumer<Personal> cambios) {
        Personal existente = obtener(codigo);
        cambios.accept(existente);
        return personal.guardar(existente);
    }

    public void darDeBaja(String codigo) {
        Personal existente = obtener(codigo);
        existente.darDeBaja();
        personal.guardar(existente);
        disponibilidades.listar(d -> d.getRecurso().getId().equals(codigo))
                .forEach(d -> disponibilidades.eliminar(d.getId()));
    }

    public Personal obtener(String codigo) {
        return personal.buscarPorId(codigo).orElseThrow(() -> new NoEncontradoException("Personal", codigo));
    }

    public List<Personal> listar(TipoPersonal tipo) {
        return personal.listar(p -> p.estaActivo() && (tipo == null || p.getTipoPersonal() == tipo));
    }
}
