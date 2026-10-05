package com.msm.eventos.aplicacion.puertos;

import com.msm.eventos.dominio.comun.Identificable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repositorio<T extends Identificable> {
    T guardar(T entidad);

    Optional<T> buscarPorId(String id);

    List<T> listar();

    void eliminar(String id);

    default List<T> listar(Predicate<? super T> filtro) {
        return listar().stream().filter(filtro).toList();
    }
}
