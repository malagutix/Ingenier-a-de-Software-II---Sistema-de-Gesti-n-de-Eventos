package modelo.eventos.exportacion;

import java.nio.charset.StandardCharsets;

import modelo.eventos.entidades.HojaServicio.Fila;
import modelo.eventos.entidades.HojaServicio.Seccion;
import modelo.eventos.entidades.HojaServicio;

/**
 * Exporta la hoja en RTF, formato que Word abre directamente, con una tabla
 * de dos columnas.
 */
public class ExportadorWord implements Exportador {

    @Override
    public String getNombre() { return "Word"; }

    @Override
    public String getExtension() { return "rtf"; }

    @Override
    public byte[] exportar(HojaServicio hoja) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\\rtf1\\ansi\\deff0{\\fonttbl{\\f0\\fswiss Arial;}}\\f0\\fs22\n");
        sb.append("{\\b\\fs36 ").append(rtf(hoja.getTitulo())).append("}\\par\n");
        sb.append("{\\fs18 ").append(rtf(hoja.getSubtitulo())).append("}\\par\n");
        for (Seccion seccion : hoja.getSecciones()) {
            sb.append("\\par{\\b\\fs26 ").append(rtf(seccion.titulo())).append("}\\par\n");
            tabla(sb, seccion.filas());
        }
        sb.append("}");
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static void tabla(StringBuilder sb, java.util.List<Fila> filas) {
        for (Fila f : filas) {
            sb.append("\\trowd\\trgaph108\\cellx3400\\cellx9400\n");
            sb.append("\\pard\\intbl{\\b ").append(rtf(f.campo())).append("}\\cell ");
            sb.append("\\pard\\intbl ").append(rtf(f.valor())).append("\\cell\\row\n");
        }
        sb.append("\\pard\n");
    }

    /** Escapa caracteres especiales y no ASCII (acentos, ñ) para RTF. */
    private static String rtf(String texto) {
        StringBuilder sb = new StringBuilder();
        for (char c : texto.toCharArray()) {
            if (c == '\\' || c == '{' || c == '}') {
                sb.append('\\').append(c);
            } else if (c > 127) {
                sb.append("\\u").append((int) (short) c).append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
