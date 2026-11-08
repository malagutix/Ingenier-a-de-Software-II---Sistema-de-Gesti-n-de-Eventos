package modelo.persistencia;

import java.util.List;

/** Resultado de consultar los registros de una tabla (CDU 007). */
public final class ConsultaTabla {

    private final List<String> columnas;
    private final List<Object[]> filas;
    private final boolean recortada;

    ConsultaTabla(List<String> columnas, List<Object[]> filas, boolean recortada) {
        this.columnas = columnas;
        this.filas = filas;
        this.recortada = recortada;
    }

    public List<String> getColumnas() { return columnas; }
    public List<Object[]> getFilas() { return filas; }
    public boolean isRecortada() { return recortada; }
}
