package com.msm.eventos.dominio.comun;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class GeneradorCodigo {
    private static final Map<String, AtomicLong> SECUENCIAS = new ConcurrentHashMap<>();

    private GeneradorCodigo() {
    }

    public static String siguiente(String prefijo) {
        long numero = SECUENCIAS.computeIfAbsent(prefijo, p -> new AtomicLong()).incrementAndGet();
        return String.format("%s-%05d", prefijo, numero);
    }
}
