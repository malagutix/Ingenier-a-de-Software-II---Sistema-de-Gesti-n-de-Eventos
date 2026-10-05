package com.msm.eventos.infraestructura.persistencia;

import com.msm.eventos.aplicacion.puertos.Repositorio;
import com.msm.eventos.dominio.comun.Identificable;
import com.msm.eventos.dominio.comun.Validar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación provisoria hasta contar con la base de datos definitiva.
 */
public class RepositorioEnMemoria<T extends Identificable> implements Repositorio<T> {
    private final Map<String, T> datos = new LinkedHashMap<>();

    @Override
    public synchronized T guardar(T entidad) {
        Validar.requerido(entidad, "entidad");
        datos.put(entidad.getId(), entidad);
        return entidad;
    }

    @Override
    public synchronized Optional<T> buscarPorId(String id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public synchronized List<T> listar() {
        return List.copyOf(datos.values());
    }

    @Override
    public synchronized void eliminar(String id) {
        datos.remove(id);
    }
}
