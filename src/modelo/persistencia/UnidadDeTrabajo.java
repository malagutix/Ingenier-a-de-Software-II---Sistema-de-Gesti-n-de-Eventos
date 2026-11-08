package modelo.persistencia;

import java.util.function.Supplier;

/**
 * Ejecuta una operación completa como una unidad: o se guarda todo o no se
 * guarda nada (transacción). Las fachadas envuelven cada operación que
 * modifica datos. Con la base de datos es una transacción de PostgreSQL; en
 * memoria simplemente ejecuta la operación.
 */
public interface UnidadDeTrabajo {

    <T> T ejecutar(Supplier<T> operacion);

    default void ejecutar(Runnable operacion) {
        ejecutar(() -> {
            operacion.run();
            return null;
        });
    }

    /** Para el modo en memoria: no hay nada que confirmar ni deshacer. */
    UnidadDeTrabajo SIN_TRANSACCION = new UnidadDeTrabajo() {
        @Override
        public <T> T ejecutar(Supplier<T> operacion) {
            return operacion.get();
        }
    };
}
