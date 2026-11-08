package modelo.eventos.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Formatos de fecha usados en pantallas y hojas de servicio. */
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Formato() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    public static String fechaHora(LocalDateTime fechaHora) {
        return fechaHora.format(FECHA_HORA);
    }

    public static String hora(LocalTime hora) {
        return hora == null ? "-" : hora.format(HORA);
    }

    /** Horario "20:00 a 03:00 (+1 día)" de un período que puede pasar la medianoche. */
    public static String horario(LocalDateTime inicio, LocalDateTime fin) {
        String texto = inicio.format(HORA) + " a " + fin.format(HORA);
        long dias = java.time.temporal.ChronoUnit.DAYS.between(inicio.toLocalDate(), fin.toLocalDate());
        return dias > 0 ? texto + " (+" + dias + " día)" : texto;
    }

    public static String minutos(int minutos) {
        if (minutos < 60) {
            return minutos + " min";
        }
        int h = minutos / 60;
        int m = minutos % 60;
        return m == 0 ? h + " h" : h + " h " + m + " min";
    }
}
