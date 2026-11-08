package vista;

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.JOptionPane;

/**
 * Comportamiento común de todas las vistas (la V de MVC). Una vista solo
 * muestra datos y avisa al controlador lo que hace el usuario; nunca llama al
 * modelo. Los mensajes y confirmaciones que el controlador necesita mostrar
 * pasan por estos métodos.
 */
public interface Vista {

    /** Componente Swing sobre el que se abren los diálogos de esta vista. */
    Component componente();

    default void mostrarError(String mensaje) {
        Ui.error(componente(), mensaje);
    }

    default void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(componente(), mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    default void mostrarInfo(String mensaje) {
        Ui.info(componente(), mensaje);
    }

    /** Pregunta Sí / No con ícono de advertencia. */
    default boolean confirmar(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(componente(), mensaje, titulo, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Pregunta Sí / No con el ícono de pregunta. */
    default boolean preguntar(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(componente(), mensaje, titulo, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /** Pregunta Sí / No / Cancelar. Devuelve true, false o null (cancelar o cerrar). */
    default Boolean preguntarSiNoCancelar(String mensaje, String titulo) {
        int respuesta = JOptionPane.showConfirmDialog(componente(), mensaje, titulo,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            return true;
        }
        return respuesta == JOptionPane.NO_OPTION ? Boolean.FALSE : null;
    }

    /** Muestra el cursor de espera mientras el controlador trabaja. */
    default void ocupado(boolean ocupado) {
        componente().setCursor(ocupado ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }
}
