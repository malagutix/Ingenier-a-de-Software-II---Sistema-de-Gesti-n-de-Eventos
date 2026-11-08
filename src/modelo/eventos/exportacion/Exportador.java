package modelo.eventos.exportacion;

import modelo.eventos.entidades.HojaServicio;

/**
 * Patrón Strategy: define cómo se exporta una hoja de servicio.
 * HojaServicio no sabe de formatos; recibe el exportador que se elija.
 */
public interface Exportador {

    String getNombre();

    String getExtension();

    byte[] exportar(HojaServicio hoja);
}
