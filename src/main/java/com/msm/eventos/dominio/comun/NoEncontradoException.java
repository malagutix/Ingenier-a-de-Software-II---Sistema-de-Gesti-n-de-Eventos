package com.msm.eventos.dominio.comun;

public class NoEncontradoException extends RuntimeException {
    public NoEncontradoException(String entidad, String id) {
        super(entidad + " no encontrado: " + id);
    }
}
