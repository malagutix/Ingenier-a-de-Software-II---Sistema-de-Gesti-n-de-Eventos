package controlador;

import java.time.YearMonth;
import java.util.List;

import modelo.eventos.entidades.Evento;
import modelo.eventos.servicio.SistemaEventos;
import modelo.usuarios.entidades.Usuario;
import vista.VistaCalendario;

/**
 * Controlador del calendario mensual. Lleva el mes que se está viendo y le
 * pide al modelo sus eventos: quien gestiona eventos ve todos, un cliente solo los suyos.
 */
public class ControladorCalendario extends Controlador implements Refrescable {

    private final VistaCalendario vista;
    private final SistemaEventos sistema;
    private final Usuario usuario;
    private YearMonth mes = YearMonth.now();

    public ControladorCalendario(VistaCalendario vista, SistemaEventos sistema, Usuario usuario) {
        this.vista = vista;
        this.sistema = sistema;
        this.usuario = usuario;
        vista.alAnterior(() -> { mes = mes.minusMonths(1); refrescar(); });
        vista.alSiguiente(() -> { mes = mes.plusMonths(1); refrescar(); });
        vista.alHoy(() -> { mes = YearMonth.now(); refrescar(); });
        refrescar();
    }

    @Override
    public void refrescar() {
        List<Evento> eventos = cargar(vista, () -> sistema.eventosDelMes(usuario, mes));
        if (eventos == null) {
            return;
        }
        Evento proximo = eventos.isEmpty() ? sistema.proximoEvento(usuario).orElse(null) : null;
        vista.mostrarMes(mes, eventos, proximo);
    }
}
