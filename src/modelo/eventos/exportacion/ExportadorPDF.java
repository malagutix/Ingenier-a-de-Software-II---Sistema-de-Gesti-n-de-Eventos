package modelo.eventos.exportacion;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import modelo.eventos.entidades.HojaServicio.Fila;
import modelo.eventos.entidades.HojaServicio.Seccion;
import modelo.eventos.entidades.HojaServicio;

/**
 * Genera un PDF real escribiendo el formato a mano (sin librerías externas).
 * Usa las fuentes Helvetica estándar de PDF con codificación WinAnsi.
 */
public class ExportadorPDF implements Exportador {

    private static final Charset WIN_ANSI = Charset.forName("windows-1252");
    private static final int ANCHO = 595;   // A4 en puntos
    private static final int ALTO = 842;
    private static final int MARGEN = 56;
    private static final int X_VALOR = 230;
    private static final int MAX_CARACTERES_VALOR = 58;

    private final List<StringBuilder> paginas = new ArrayList<>();
    private StringBuilder actual;
    private int y;

    @Override
    public String getNombre() { return "PDF"; }

    @Override
    public String getExtension() { return "pdf"; }

    @Override
    public synchronized byte[] exportar(HojaServicio hoja) {
        paginas.clear();
        nuevaPagina();

        texto("F2", 20, MARGEN, y, hoja.getTitulo());
        y -= 20;
        texto("F1", 10, MARGEN, y, hoja.getSubtitulo());
        y -= 24;

        for (Seccion seccion : hoja.getSecciones()) {
            if (y - 40 < MARGEN) {
                nuevaPagina();
            }
            texto("F2", 13, MARGEN, y, seccion.titulo());
            y -= 7;
            linea();
            y -= 17;
            for (Fila fila : seccion.filas()) {
                filaConValor(fila, 10);
            }
            y -= 10;
        }
        y -= 30;
        texto("F1", 9, MARGEN, y, "Sistema de Gestión de Eventos - Subsistema 1");
        return armarDocumento();
    }

    private void filaConValor(Fila fila, int tamano) {
        List<String> renglones = partir(fila.valor(), MAX_CARACTERES_VALOR);
        if (y - renglones.size() * 15 < MARGEN) {
            nuevaPagina();
        }
        texto("F2", tamano, MARGEN, y, fila.campo());
        for (String renglon : renglones) {
            texto("F1", tamano, X_VALOR, y, renglon);
            y -= 14;
        }
        y -= 3;
    }

    private void nuevaPagina() {
        actual = new StringBuilder();
        paginas.add(actual);
        y = ALTO - MARGEN;
    }

    private void texto(String fuente, int tamano, int x, int yPos, String s) {
        actual.append("BT /").append(fuente).append(' ').append(tamano).append(" Tf 1 0 0 1 ")
                .append(x).append(' ').append(yPos).append(" Tm (").append(escapar(s)).append(") Tj ET\n");
    }

    private void linea() {
        actual.append("0.75 G 0.8 w ").append(MARGEN).append(' ').append(y).append(" m ")
                .append(ANCHO - MARGEN).append(' ').append(y).append(" l S 0 G\n");
    }

    private byte[] armarDocumento() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        escribir(out, "%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n");

        int cantidadPaginas = paginas.size();
        int primerObjetoPagina = 5;
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < cantidadPaginas; i++) {
            kids.append(primerObjetoPagina + i * 2).append(" 0 R ");
        }

        objeto(out, offsets, "<< /Type /Catalog /Pages 2 0 R >>");
        objeto(out, offsets, "<< /Type /Pages /Kids [" + kids + "] /Count " + cantidadPaginas + " >>");
        objeto(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        objeto(out, offsets, "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < cantidadPaginas; i++) {
            int contenido = primerObjetoPagina + i * 2 + 1;
            objeto(out, offsets, "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + ANCHO + " " + ALTO + "] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents " + contenido + " 0 R >>");
            byte[] stream = paginas.get(i).toString().getBytes(WIN_ANSI);
            offsets.add(out.size());
            escribir(out, offsets.size() + " 0 obj\n<< /Length " + stream.length + " >>\nstream\n");
            out.write(stream, 0, stream.length);
            escribir(out, "endstream\nendobj\n");
        }

        int inicioXref = out.size();
        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(offsets.size() + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            xref.append(String.format("%010d 00000 n \n", offset));
        }
        xref.append("trailer\n<< /Size ").append(offsets.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(inicioXref).append("\n%%EOF\n");
        escribir(out, xref.toString());
        return out.toByteArray();
    }

    private static void objeto(ByteArrayOutputStream out, List<Integer> offsets, String contenido) {
        offsets.add(out.size());
        escribir(out, offsets.size() + " 0 obj\n" + contenido + "\nendobj\n");
    }

    private static void escribir(ByteArrayOutputStream out, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes, 0, bytes.length);
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == ')' || c == '\\') {
                sb.append('\\');
            }
            sb.append(WIN_ANSI.newEncoder().canEncode(c) ? c : '?');
        }
        return sb.toString();
    }

    private static List<String> partir(String texto, int maximo) {
        List<String> renglones = new ArrayList<>();
        StringBuilder renglon = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (renglon.length() > 0 && renglon.length() + palabra.length() + 1 > maximo) {
                renglones.add(renglon.toString());
                renglon.setLength(0);
            }
            if (renglon.length() > 0) {
                renglon.append(' ');
            }
            renglon.append(palabra);
        }
        renglones.add(renglon.toString());
        return renglones;
    }
}
