package modelo.eventos.entidades;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lugar donde se realiza un evento. Registra capacidad máxima, disponibilidad
 * (habilitada y fechas bloqueadas), distancia a la sede, tiempo de traslado y
 * requerimientos logísticos (vehículos, cámara de frío, tiempo de montaje).
 */
public class Locacion {

    private int idLocacion;
    private String nombre;
    private String direccion;
    private TipoLocacion tipo;
    private int capacidad;
    private double distanciaKm;
    private int tiempoTrasladoMin;
    private int tiempoMontajeMin;
    private int vehiculosRequeridos;
    private boolean requiereCamaraFrio;
    private String otrosRequerimientos;
    private boolean habilitada = true;
    private final List<BloqueoFecha> bloqueos = new ArrayList<>();

    public Locacion(String nombre, String direccion, TipoLocacion tipo, int capacidad, double distanciaKm,
                    int tiempoTrasladoMin, int tiempoMontajeMin, int vehiculosRequeridos,
                    boolean requiereCamaraFrio, String otrosRequerimientos) {
        modificar(nombre, direccion, tipo, capacidad, distanciaKm, tiempoTrasladoMin, tiempoMontajeMin,
                vehiculosRequeridos, requiereCamaraFrio, otrosRequerimientos);
    }

    public final void modificar(String nombre, String direccion, TipoLocacion tipo, int capacidad,
                                double distanciaKm, int tiempoTrasladoMin, int tiempoMontajeMin,
                                int vehiculosRequeridos, boolean requiereCamaraFrio, String otrosRequerimientos) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.distanciaKm = distanciaKm;
        this.tiempoTrasladoMin = tiempoTrasladoMin;
        this.tiempoMontajeMin = tiempoMontajeMin;
        this.vehiculosRequeridos = vehiculosRequeridos;
        this.requiereCamaraFrio = requiereCamaraFrio;
        this.otrosRequerimientos = otrosRequerimientos;
    }

    public void habilitar() { this.habilitada = true; }
    public void inhabilitar() { this.habilitada = false; }

    public void agregarBloqueo(BloqueoFecha bloqueo) { bloqueos.add(bloqueo); }
    public void quitarBloqueo(BloqueoFecha bloqueo) { bloqueos.remove(bloqueo); }

    /** Reemplaza los bloqueos por los leídos de la base de datos (solo para la persistencia). */
    public void restaurarBloqueos(List<BloqueoFecha> guardados) {
        bloqueos.clear();
        bloqueos.addAll(guardados);
    }

    /** Devuelve el bloqueo que incluye esa fecha, o null si está libre. */
    public BloqueoFecha bloqueoEn(LocalDate fecha) {
        for (BloqueoFecha b : bloqueos) {
            if (b.incluye(fecha)) {
                return b;
            }
        }
        return null;
    }

    public int getIdLocacion() { return idLocacion; }
    public void setIdLocacion(int idLocacion) { this.idLocacion = idLocacion; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public TipoLocacion getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    public double getDistanciaKm() { return distanciaKm; }
    public int getTiempoTrasladoMin() { return tiempoTrasladoMin; }
    public int getTiempoMontajeMin() { return tiempoMontajeMin; }
    public int getVehiculosRequeridos() { return vehiculosRequeridos; }
    public boolean isRequiereCamaraFrio() { return requiereCamaraFrio; }
    public String getOtrosRequerimientos() { return otrosRequerimientos; }
    public boolean isHabilitada() { return habilitada; }
    public List<BloqueoFecha> getBloqueos() { return Collections.unmodifiableList(bloqueos); }

    @Override
    public String toString() {
        return nombre + " (hasta " + capacidad + " personas)";
    }
}
