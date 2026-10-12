package cl.duoc.biblioteca;

import cl.duoc.biblioteca.view.LoginView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // se usa el aspecto por defecto
            }
            new LoginView().setVisible(true);
        });
    }
}
