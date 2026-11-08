package modelo.eventos.exportacion;

import java.nio.charset.StandardCharsets;

import modelo.eventos.entidades.HojaServicio.Fila;
import modelo.eventos.entidades.HojaServicio.Seccion;
import modelo.eventos.entidades.HojaServicio;

/**
 * Exporta la hoja como CSV separado por punto y coma, que Excel abre
 * directamente (con configuración regional en español).
 */
public class ExportadorExcel implements Exportador {

    @Override
    public String getNombre() { return "Excel"; }

    @Override
    public String getExtension() { return "csv"; }

    @Override
    public byte[] exportar(HojaServicio hoja) {
        StringBuilder sb = new StringBuilder("\uFEFF"); // BOM para que Excel detecte UTF-8
        sb.append(celda(hoja.getTitulo())).append("\r\n");
        sb.append(celda(hoja.getSubtitulo())).append("\r\n");
        for (Seccion seccion : hoja.getSecciones()) {
            sb.append("\r\n").append(celda(seccion.titulo().toUpperCase())).append(";\r\n");
            for (Fila f : seccion.filas()) {
                sb.append(celda(f.campo())).append(';').append(celda(f.valor())).append("\r\n");
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String celda(String valor) {
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
