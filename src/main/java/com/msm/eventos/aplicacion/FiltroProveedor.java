package com.msm.eventos.aplicacion;

import com.msm.eventos.dominio.proveedor.Proveedor;
import com.msm.eventos.dominio.servicio.Servicio;

public record FiltroProveedor(String rubro, Class<? extends Servicio> tipoServicio, boolean soloConServiciosDisponibles) {

    public static FiltroProveedor todos() {
        return new FiltroProveedor(null, null, false);
    }

    public boolean cumple(Proveedor proveedor) {
        return proveedor.estaActivo()
                && (rubro == null || proveedor.getRubro().equalsIgnoreCase(rubro))
                && (tipoServicio == null || proveedor.ofrece(tipoServicio))
                && (!soloConServiciosDisponibles || proveedor.tieneServiciosDisponibles());
    }
}
