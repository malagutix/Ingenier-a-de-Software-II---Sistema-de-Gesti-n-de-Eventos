package com.msm.eventos.dominio.comun;

import java.time.Duration;
import java.time.LocalDateTime;

public record FranjaHoraria(LocalDateTime inicio, LocalDateTime fin) {

    public FranjaHoraria {
        Validar.requerido(inicio, "inicio");
        Validar.requerido(fin, "fin");
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("La franja horaria debe terminar después de comenzar");
        }
    }

    public boolean seSolapaCon(FranjaHoraria otra) {
        return inicio.isBefore(otra.fin) && otra.inicio.isBefore(fin);
    }

    public boolean contiene(FranjaHoraria otra) {
        return !otra.inicio.isBefore(inicio) && !otra.fin.isAfter(fin);
    }

    public double horas() {
        return Duration.between(inicio, fin).toMinutes() / 60.0;
    }
}
