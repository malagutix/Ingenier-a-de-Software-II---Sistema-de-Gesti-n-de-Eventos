package modelo.eventos.entidades;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import modelo.eventos.exportacion.Exportador;
import modelo.usuarios.entidades.Usuario;

/**
 * Hoja de servicio de un evento (RF017 / CDU 015). Incluye, como pide el
 * enunciado, el menú, la locación, fechas y horarios de inicio y fin, y el
 * resumen de consumos y recursos logísticos. Se exporta con un Exportador
 * (Strategy). Los proveedores, chefs y personal los asigna el Subsistema 2.
 * Los precios no se incluyen: su cálculo corresponde al Subsistema 2.
 */
public class HojaServicio {

    /** Una fila "campo: valor" de la hoja. */
    public static final class Fila {
        private final String campo;
        private final String valor;

        public Fila(String campo, String valor) {
            this.campo = campo;
            this.valor = valor;
        }

        public String campo() { return campo; }
        public String valor() { return valor; }
    }

    /** Un bloque de la hoja con su título y sus filas. */
    public static final class Seccion {
        private final String titulo;
        private final List<Fila> filas = new ArrayList<>();

        Seccion(String titulo) {
            this.titulo = titulo;
        }

        Seccion fila(String campo, String valor) {
            filas.add(new Fila(campo, valor));
            return this;
        }

        public String titulo() { return titulo; }
        public List<Fila> filas() { return Collections.unmodifiableList(filas); }
    }

    private static final String PENDIENTE_S2 = "Pendiente de asignación (Subsistema 2)";

    private int idHoja;
    private final LocalDateTime fechaGeneracion;
    private final int idEvento;
    private final List<Seccion> secciones = new ArrayList<>();

    HojaServicio(Evento evento) {
        this.fechaGeneracion = LocalDateTime.now();
        this.idEvento = evento.getIdEvento();

        Planificacion planificacion = evento.getPlanificacion();
        Locacion locacion = evento.getLocacion();
        int montaje = locacion.getTiempoMontajeMin();

        secciones.add(new Seccion("Datos del evento")
                .fila("Evento", "#" + evento.getIdEvento())
                .fila("Tipo de evento", evento.getTipo().getNombre())
                .fila("Estado", evento.getEstado().getNombre())
                .fila("Fecha", Formato.fecha(evento.getFecha()))
                .fila("Horario", evento.getHorario())
                .fila("Duración", evento.getDuracionHoras() + " horas")
                .fila("Cantidad de asistentes", String.valueOf(evento.getCantidadAsistentes()))
                .fila("Requerimientos específicos", texto(evento.getRequerimientosEspecificos()))
                .fila("Planificación", "#" + planificacion.getIdPlanificacion() + " " + planificacion.getNombre()
                        + " (evento " + planificacion.posicionDe(evento) + ")")
                .fila("Solicitud de origen", "#" + evento.getSolicitud().getIdSolicitud())
                .fila("Cliente", persona(evento.getCliente()))
                .fila("Organizador", persona(evento.getOrganizador())));

        secciones.add(new Seccion("Locación")
                .fila("Nombre", locacion.getNombre())
                .fila("Tipo", locacion.getTipo().toString())
                .fila("Dirección", locacion.getDireccion())
                .fila("Capacidad máxima", locacion.getCapacidad() + " personas")
                .fila("Distancia a la sede", formatoKm(locacion.getDistanciaKm()))
                .fila("Tiempo de traslado", Formato.minutos(locacion.getTiempoTrasladoMin())));

        secciones.add(new Seccion("Recursos logísticos")
                .fila("Salida desde la sede", Formato.fechaHora(evento.getInicio()
                        .minusMinutes(montaje + locacion.getTiempoTrasladoMin())))
                .fila("Inicio del montaje", Formato.fechaHora(evento.getInicio().minusMinutes(montaje)))
                .fila("Fin del desmontaje", Formato.fechaHora(evento.getFin().plusMinutes(montaje)))
                .fila("Tiempo de montaje", Formato.minutos(montaje))
                .fila("Vehículos", String.valueOf(locacion.getVehiculosRequeridos()))
                .fila("Cámara de frío", locacion.isRequiereCamaraFrio() ? "Sí" : "No")
                .fila("Otros requerimientos", texto(locacion.getOtrosRequerimientos())));

        secciones.add(new Seccion("Menú y consumos")
                .fila("Menú seleccionado", evento.getMenu().toString())
                .fila("Cubiertos estimados", evento.getMenu() == MenuEvento.SIN_MENU
                        ? "0" : String.valueOf(evento.getCantidadAsistentes())));

        secciones.add(new Seccion("Proveedores y personal")
                .fila("Proveedores / chefs", PENDIENTE_S2)
                .fila("Personal requerido", PENDIENTE_S2));
    }

    /** Strategy: el formato depende del exportador que se pase. */
    public byte[] exportar(Exportador exportador) {
        return exportador.exportar(this);
    }

    public String getTitulo() {
        return "Hoja de servicio - Evento #" + idEvento;
    }

    public String getSubtitulo() {
        return "Hoja #" + idHoja + " - generada el " + Formato.fechaHora(fechaGeneracion);
    }

    public String getDetalle() {
        StringBuilder sb = new StringBuilder();
        sb.append(getTitulo()).append('\n').append(getSubtitulo()).append('\n');
        for (Seccion seccion : secciones) {
            sb.append('\n').append(seccion.titulo().toUpperCase()).append('\n');
            for (Fila f : seccion.filas()) {
                sb.append(String.format("  %-28s %s%n", f.campo() + ":", f.valor()));
            }
        }
        return sb.toString();
    }

    public int getIdHoja() { return idHoja; }
    /** Lo asigna el repositorio al guardar la hoja. */
    public void setIdHoja(int idHoja) { this.idHoja = idHoja; }
    public int getIdEvento() { return idEvento; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public List<Seccion> getSecciones() { return Collections.unmodifiableList(secciones); }

    private static String persona(Usuario u) {
        return u.getNombre() + " " + u.getApellido() + " (" + u.getCorreoElectronico() + ")";
    }

    private static String texto(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s;
    }

    private static String formatoKm(double km) {
        return (km == Math.floor(km) ? String.valueOf((long) km) : String.valueOf(km)).replace('.', ',') + " km";
    }
}
