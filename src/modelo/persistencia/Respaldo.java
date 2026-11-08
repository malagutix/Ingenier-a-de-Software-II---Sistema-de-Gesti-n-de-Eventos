package modelo.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Respaldo y restauración de datos (RNF 003). El archivo es texto: por cada
 * tabla una línea "@tabla columnas" y luego una fila por línea, con los
 * valores separados por tabulaciones.
 *
 * Al restaurar no se ejecuta SQL del archivo: se validan los nombres de
 * tablas y columnas contra el esquema y los valores se pasan como parámetros
 * (así un archivo alterado no puede inyectar SQL).
 */
class Respaldo {

    private static final String ENCABEZADO = "# Respaldo - Sistema de Gestión de Eventos";
    private static final String NULO = "\\N";
    private static final DateTimeFormatter NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    static final int RESPALDOS_A_CONSERVAR = 7;

    private final ContextoPersistencia ctx;

    Respaldo(ContextoPersistencia ctx) {
        this.ctx = ctx;
    }

    /** Escribe un respaldo completo en el archivo indicado. */
    void respaldar(Path archivo) {
        ctx.enTransaccion(c -> {
            try (BufferedWriter w = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
                w.write(ENCABEZADO + "\n");
                w.write("# esquema=" + Esquema.VERSION + "\n");
                w.write("# generado=" + LocalDateTime.now().withNano(0) + "\n");
                for (String tabla : Esquema.TABLAS) {
                    volcarTabla(c, tabla, w);
                }
            } catch (IOException e) {
                throw new ErrorBaseDatosException("No se pudo escribir el respaldo en " + archivo + ": "
                        + e.getMessage(), e);
            }
            try (PreparedStatement ps = c.prepareStatement("UPDATE control SET ultimo_respaldo = ? WHERE id = 1")) {
                ps.setObject(1, LocalDateTime.now());
                ps.executeUpdate();
            }
            return null;
        });
    }

    private static void volcarTabla(Connection c, String tabla, BufferedWriter w) throws SQLException, IOException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + tabla + " ORDER BY 1")) {
            ResultSetMetaData md = rs.getMetaData();
            int n = md.getColumnCount();
            StringBuilder cab = new StringBuilder("@").append(tabla).append(' ');
            for (int i = 1; i <= n; i++) {
                cab.append(i > 1 ? "," : "").append(md.getColumnName(i));
            }
            w.write(cab.append('\n').toString());
            while (rs.next()) {
                StringBuilder fila = new StringBuilder();
                for (int i = 1; i <= n; i++) {
                    if (i > 1) {
                        fila.append('\t');
                    }
                    String valor = rs.getString(i);
                    fila.append(valor == null ? NULO : escapar(valor));
                }
                w.write(fila.append('\n').toString());
            }
        }
    }

    /** Reemplaza todos los datos por los del respaldo. Es todo o nada. */
    void restaurar(Path archivo) {
        List<Bloque> bloques = leer(archivo);
        ctx.escribir(c -> {
            Set<String> tablasValidas = new HashSet<>(Esquema.TABLAS);
            try (Statement st = c.createStatement()) {
                st.execute("TRUNCATE " + String.join(", ", Esquema.TABLAS) + " RESTART IDENTITY CASCADE");
            }
            for (Bloque b : bloques) {
                if (!tablasValidas.contains(b.tabla)) {
                    throw new ErrorBaseDatosException("El respaldo tiene una tabla desconocida: " + b.tabla);
                }
                Set<String> columnasValidas = columnas(c, b.tabla);
                for (String col : b.columnas) {
                    if (!columnasValidas.contains(col)) {
                        throw new ErrorBaseDatosException("El respaldo tiene una columna desconocida: "
                                + b.tabla + "." + col);
                    }
                }
                StringBuilder sql = new StringBuilder("INSERT INTO ").append(b.tabla).append(" (")
                        .append(String.join(", ", b.columnas)).append(") VALUES (");
                for (int i = 0; i < b.columnas.size(); i++) {
                    sql.append(i > 0 ? ", ?" : "?");
                }
                try (PreparedStatement ps = c.prepareStatement(sql.append(')').toString())) {
                    for (String[] fila : b.filas) {
                        for (int i = 0; i < fila.length; i++) {
                            // Types.OTHER: PostgreSQL convierte el texto al tipo de la columna
                            ps.setObject(i + 1, fila[i], Types.OTHER);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            for (String tabla : Esquema.CON_SECUENCIA) {
                try (Statement st = c.createStatement()) {
                    st.execute("SELECT setval(pg_get_serial_sequence('" + tabla + "', 'id'),"
                            + " COALESCE((SELECT MAX(id) FROM " + tabla + "), 0) + 1, false)");
                }
            }
            return null;
        });
        ctx.invalidar();
    }

    private static Set<String> columnas(Connection c, String tabla) throws SQLException {
        Set<String> resultado = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT column_name FROM information_schema.columns"
                + " WHERE table_schema = current_schema() AND table_name = ?")) {
            ps.setString(1, tabla);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(rs.getString(1));
                }
            }
        }
        return resultado;
    }

    private static final class Bloque {
        private final String tabla;
        private final List<String> columnas;
        private final List<String[]> filas = new ArrayList<>();

        Bloque(String tabla, List<String> columnas) {
            this.tabla = tabla;
            this.columnas = columnas;
        }
    }

    private static List<Bloque> leer(Path archivo) {
        List<Bloque> bloques = new ArrayList<>();
        try (BufferedReader r = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea = r.readLine();
            if (linea == null || !linea.startsWith(ENCABEZADO)) {
                throw new ErrorBaseDatosException("El archivo no es un respaldo de este sistema");
            }
            Bloque actual = null;
            int numero = 1;
            while ((linea = r.readLine()) != null) {
                numero++;
                if (linea.startsWith("# esquema=") && !linea.equals("# esquema=" + Esquema.VERSION)) {
                    throw new ErrorBaseDatosException("El respaldo es de otra versión del esquema");
                }
                if (linea.startsWith("#") || linea.isEmpty()) {
                    continue;
                }
                if (linea.startsWith("@")) {
                    String[] partes = linea.substring(1).split(" ", 2);
                    List<String> cols = new ArrayList<>();
                    for (String col : partes[1].split(",")) {
                        cols.add(col.trim());
                    }
                    actual = new Bloque(partes[0], cols);
                    bloques.add(actual);
                    continue;
                }
                if (actual == null) {
                    throw new ErrorBaseDatosException("Respaldo dañado en la línea " + numero);
                }
                String[] valores = linea.split("\t", -1);
                if (valores.length != actual.columnas.size()) {
                    throw new ErrorBaseDatosException("Respaldo dañado en la línea " + numero);
                }
                for (int i = 0; i < valores.length; i++) {
                    valores[i] = NULO.equals(valores[i]) ? null : desescapar(valores[i]);
                }
                actual.filas.add(valores);
            }
        } catch (IOException e) {
            throw new ErrorBaseDatosException("No se pudo leer el respaldo: " + e.getMessage(), e);
        }
        return bloques;
    }

    static String escapar(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    static String desescapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\' && i + 1 < s.length()) {
                char sig = s.charAt(++i);
                sb.append(sig == 't' ? '\t' : sig == 'n' ? '\n' : sig == 'r' ? '\r' : sig);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Nombre para un respaldo nuevo en la carpeta. */
    static Path archivoNuevo(Path carpeta) {
        return carpeta.resolve("respaldo-" + LocalDateTime.now().format(NOMBRE_ARCHIVO) + ".txt");
    }

    /** Respaldos de la carpeta, del más nuevo al más viejo. */
    static List<Path> respaldosEn(Path carpeta) {
        List<Path> lista = new ArrayList<>();
        if (!Files.isDirectory(carpeta)) {
            return lista;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(carpeta, "respaldo-*.txt")) {
            for (Path p : ds) {
                lista.add(p);
            }
        } catch (IOException e) {
            return lista;
        }
        lista.sort((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));
        return lista;
    }

    /** Borra los respaldos automáticos más viejos. */
    static void depurar(Path carpeta) {
        List<Path> lista = respaldosEn(carpeta);
        for (int i = RESPALDOS_A_CONSERVAR; i < lista.size(); i++) {
            try {
                Files.deleteIfExists(lista.get(i));
            } catch (IOException ignorada) {
                // se intenta la próxima vez
            }
        }
    }
}
