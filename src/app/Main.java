package app;

import javax.swing.SwingUtilities;

import controlador.ControladorAplicacion;

/**
 * Punto de entrada. La aplicación sigue el patrón MVC:
 * - modelo: entidades, reglas del negocio, repositorios y persistencia (no conoce la interfaz);
 * - vista: pantallas Swing que solo muestran datos y avisan lo que hace el usuario;
 * - controlador: recibe esas acciones, le pide al modelo que las realice y actualiza la vista.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ControladorAplicacion().iniciar());
    }
}
