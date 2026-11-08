package modelo.eventos.repositorio;

import java.util.ArrayList;
import java.util.List;

import modelo.eventos.entidades.HojaServicio;

public class HojaServicioRepositoryEnMemoria implements HojaServicioRepository {

    private final List<HojaServicio> hojas = new ArrayList<>();

    @Override
    public synchronized void guardar(HojaServicio hoja) {
        hoja.setIdHoja(hojas.size() + 1);
        hojas.add(hoja);
    }
}
