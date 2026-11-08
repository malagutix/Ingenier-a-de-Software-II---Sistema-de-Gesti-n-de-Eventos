package vista;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import modelo.eventos.entidades.Evento;
import modelo.eventos.entidades.Formato;
import modelo.eventos.entidades.Planificacion;
import modelo.eventos.entidades.SolicitudEvento;

/**
 * Diálogo para planificar uno o varios eventos (en días consecutivos) a partir
 * de una solicitud. Si la solicitud ya tiene planificación, los eventos se
 * agregan a ella. Lo usan las vistas de Planificaciones y de Eventos.
 */
public final class DialogoPlanificar {

    private DialogoPlanificar() {
    }

    /** Una solicitud que se puede planificar, con los datos que el controlador obtuvo del modelo. */
    public static final class OpcionSolicitud {
        private final SolicitudEvento solicitud;
        private final Planificacion planificacion;
        private final LocalDate fechaSugerida;

        /** @param planificacion la planificación que ya tiene la solicitud, o null */
        public OpcionSolicitud(SolicitudEvento solicitud, Planificacion planificacion, LocalDate fechaSugerida) {
            this.solicitud = solicitud;
            this.planificacion = planificacion;
            this.fechaSugerida = fechaSugerida;
        }

        @Override
        public String toString() {
            String texto = "#" + solicitud.getIdSolicitud() + "  " + Formato.fecha(solicitud.getFechaSolicitada())
                    + "  " + solicitud.getTipoEvento().getNombre() + ", " + solicitud.getCantidadInvitados() + " inv., "
                    + solicitud.getLocacion().getNombre() + " - " + solicitud.getCliente().getNombre() + " "
                    + solicitud.getCliente().getApellido();
            return planificacion == null ? texto
                    : texto + "   [ya planificada: " + planificacion.getCantidadEventos() + " evento(s)]";
        }
    }

    /** Lo que cargó el usuario en el diálogo. */
    public static final class Pedido {
        private final int idSolicitud;
        private final LocalDate fechaInicio;
        private final LocalTime horaInicio;
        private final int duracionHoras;
        private final int cantidadDias;
        private final String nombre;

        Pedido(int idSolicitud, LocalDate fechaInicio, LocalTime horaInicio, int duracionHoras, int cantidadDias,
               String nombre) {
            this.idSolicitud = idSolicitud;
            this.fechaInicio = fechaInicio;
            this.horaInicio = horaInicio;
            this.duracionHoras = duracionHoras;
            this.cantidadDias = cantidadDias;
            this.nombre = nombre;
        }

        public int idSolicitud() { return idSolicitud; }
        public LocalDate fechaInicio() { return fechaInicio; }
        public LocalTime horaInicio() { return horaInicio; }
        public int duracionHoras() { return duracionHoras; }
        public int cantidadDias() { return cantidadDias; }
        public String nombre() { return nombre; }
    }

    /**
     * Muestra el diálogo. Se repite hasta que el controlador acepte el pedido o se cancele.
     *
     * @param idSolicitud solicitud a preseleccionar, o null
     * @return true si se planificó
     */
    static boolean mostrar(Component padre, List<OpcionSolicitud> opciones, Integer idSolicitud,
                           Predicate<Pedido> alAceptar) {
        JComboBox<OpcionSolicitud> combo = new JComboBox<>();
        for (OpcionSolicitud opcion : opciones) {
            combo.addItem(opcion);
            if (idSolicitud != null && opcion.solicitud.getIdSolicitud() == idSolicitud) {
                combo.setSelectedItem(opcion);
            }
        }
        JTextField nombre = new JTextField(28);
        JSpinner fechaInicio = Ui.selectorFecha(opciones.get(0).solicitud.getFechaSolicitada());
        JSpinner dias = Ui.selectorNumero(1, 1, 30);
        JSpinner hora = Ui.selectorHora(null);
        JSpinner duracion = Ui.selectorNumero(4, 1, 24);
        JLabel ayuda = Ui.secundario(" ");

        Runnable actualizar = () -> {
            OpcionSolicitud o = (OpcionSolicitud) combo.getSelectedItem();
            Ui.setFecha(fechaInicio, o.fechaSugerida);
            // Hora y duración: las del último evento de la planificación, o las sugeridas por el tipo
            List<Evento> previos = o.planificacion != null
                    ? o.planificacion.getEventos() : Collections.<Evento>emptyList();
            if (!previos.isEmpty()) {
                Evento ultimo = previos.get(previos.size() - 1);
                Ui.setHora(hora, ultimo.getHoraInicio());
                duracion.setValue(ultimo.getDuracionHoras());
            } else {
                Ui.setHora(hora, o.solicitud.getTipoEvento().getHoraInicioSugerida());
                duracion.setValue(o.solicitud.getTipoEvento().getDuracionSugerida());
            }
            if (o.planificacion != null) {
                nombre.setText(o.planificacion.getNombre());
                nombre.setEnabled(false);
                ayuda.setText("<html>Los eventos se agregan a la planificación existente.<br>"
                        + "Se crea un evento por día, a la misma hora y en la locación de la solicitud.</html>");
            } else {
                nombre.setText("");
                nombre.setEnabled(true);
                ayuda.setText("<html>Se crea una planificación nueva (si dejás el nombre vacío se genera uno).<br>"
                        + "Se crea un evento por día, a la misma hora y en la locación de la solicitud.</html>");
            }
        };
        combo.addActionListener(e -> actualizar.run());
        actualizar.run();

        JPanel formulario = new JPanel(new GridBagLayout());
        Ui.fila(formulario, 0, "Solicitud", combo);
        Ui.fila(formulario, 1, "Nombre de la planificación", nombre);
        Ui.fila(formulario, 2, "Fecha del primer evento", fechaInicio);
        Ui.fila(formulario, 3, "Cantidad de días", dias);
        Ui.fila(formulario, 4, "Hora de inicio", hora);
        Ui.fila(formulario, 5, "Duración (horas)", duracion);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 6;
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(8, 0, 0, 0);
        formulario.add(ayuda, c);

        return Ui.dialogoHastaExito(padre, formulario, "Planificar eventos", () -> {
            OpcionSolicitud o = (OpcionSolicitud) combo.getSelectedItem();
            return new Pedido(o.solicitud.getIdSolicitud(), Ui.getFecha(fechaInicio), Ui.getHora(hora),
                    Ui.getNumero(duracion), Ui.getNumero(dias), nombre.getText());
        }, alAceptar);
    }
}
