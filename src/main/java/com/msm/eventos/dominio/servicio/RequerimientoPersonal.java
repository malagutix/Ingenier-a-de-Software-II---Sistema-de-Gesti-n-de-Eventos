package com.msm.eventos.dominio.servicio;

import com.msm.eventos.dominio.comun.Validar;
import com.msm.eventos.dominio.recurso.TipoPersonal;

/**
 * Si invitadosPorUnidad es mayor a 0 se requiere una persona cada esa cantidad de invitados,
 * respetando siempre cantidadMinima.
 */
public record RequerimientoPersonal(TipoPersonal tipoPersonal, int cantidadMinima, int invitadosPorUnidad) {

    public RequerimientoPersonal {
        Validar.requerido(tipoPersonal, "tipoPersonal");
        Validar.noNegativo(cantidadMinima, "cantidadMinima");
        Validar.noNegativo(invitadosPorUnidad, "invitadosPorUnidad");
    }

    public static RequerimientoPersonal fijo(TipoPersonal tipo, int cantidad) {
        return new RequerimientoPersonal(tipo, cantidad, 0);
    }

    public static RequerimientoPersonal cadaInvitados(TipoPersonal tipo, int invitadosPorUnidad) {
        return new RequerimientoPersonal(tipo, 1, invitadosPorUnidad);
    }

    public int cantidadPara(int invitados) {
        if (invitadosPorUnidad == 0) {
            return cantidadMinima;
        }
        int proporcional = (int) Math.ceil((double) invitados / invitadosPorUnidad);
        return Math.max(cantidadMinima, proporcional);
    }
}
