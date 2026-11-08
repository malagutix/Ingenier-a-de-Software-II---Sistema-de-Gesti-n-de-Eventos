package modelo.eventos.repositorio;

import modelo.eventos.entidades.HojaServicio;

/** Patrón Repository para las hojas de servicio generadas (queda registro de cada una). */
public interface HojaServicioRepository {
    /** Guarda la hoja y le asigna su ID único. */
    void guardar(HojaServicio hoja);
}
