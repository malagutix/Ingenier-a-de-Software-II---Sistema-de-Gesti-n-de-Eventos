package vista;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

/**
 * Tabla en la que se pueden seleccionar varias filas haciendo clic en cada
 * una (no hace falta mantener Ctrl): un clic agrega la fila a la selección y
 * otro clic la quita. Shift+clic selecciona un rango y el doble clic deja
 * seleccionada solo esa fila (para abrirla).
 */
class TablaSeleccionMultiple extends JTable {

    private static final long serialVersionUID = 1L;

    /** true mientras se procesa un clic simple con el botón izquierdo. */
    private boolean alternar;

    TablaSeleccionMultiple(TableModel modelo) {
        super(modelo);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        alternar = e.getID() == MouseEvent.MOUSE_PRESSED && e.getClickCount() == 1
                && SwingUtilities.isLeftMouseButton(e) && !e.isShiftDown();
        try {
            super.processMouseEvent(e);
        } finally {
            alternar = false;
        }
    }

    @Override
    public void changeSelection(int fila, int columna, boolean toggle, boolean extend) {
        super.changeSelection(fila, columna, toggle || (alternar && !extend), extend);
    }
}
