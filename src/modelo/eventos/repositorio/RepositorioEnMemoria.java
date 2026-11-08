package modelo.eventos.repositorio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Base común para los repositorios en memoria. Se reemplaza por JDBC/JPA
 * cuando tengan la base de datos, sin tocar SistemaEventos.
 */
abstract class RepositorioEnMemoria<T> {

    private final Map<Integer, T> datos = new LinkedHashMap<>();
    private final Function<T, Integer> obtenerId;
    private int secuencia = 1;

    RepositorioEnMemoria(Function<T, Integer> obtenerId) {
        this.obtenerId = obtenerId;
    }

    /** Asigna el ID único al objeto. */
    protected abstract void asignarId(T objeto, int id);

    public synchronized void guardar(T objeto) {
        asignarId(objeto, secuencia++);
        datos.put(obtenerId.apply(objeto), objeto);
    }

    public synchronized Optional<T> buscarPorId(int id) {
        return Optional.ofNullable(datos.get(id));
    }

    public synchronized List<T> listarTodos() {
        return new ArrayList<>(datos.values());
    }

    public synchronized void actualizar(T objeto) {
        datos.put(obtenerId.apply(objeto), objeto);
    }

    public synchronized void eliminar(int id) {
        datos.remove(id);
    }
}
