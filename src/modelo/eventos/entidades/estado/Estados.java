package modelo.eventos.entidades.estado;

import java.util.Arrays;
import java.util.List;

/** Obtiene el objeto estado a partir de su nombre (por ejemplo, al leerlo de la base de datos). */
public final class Estados {

    private static final List<EstadoEvento> TODOS = Arrays.<EstadoEvento>asList(
            new EstadoPendienteDeReserva(), new EstadoAceptado(), new EstadoReprogramado(),
            new EstadoEnProgreso(), new EstadoFinalizado(), new EstadoCancelado());

    private Estados() {
    }

    public static EstadoEvento desdeNombre(String nombre) {
        for (EstadoEvento e : TODOS) {
            if (e.getNombre().equals(nombre)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de evento desconocido: " + nombre);
    }

    public static List<EstadoEvento> todos() {
        return TODOS;
    }
}
