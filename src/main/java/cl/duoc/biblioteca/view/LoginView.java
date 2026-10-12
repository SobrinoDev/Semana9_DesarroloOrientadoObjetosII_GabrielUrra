package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LoginController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

    private final LoginController controller = new LoginController();
    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);

    public LoginView() {
        super("Biblioteca Escolar - Ingreso");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.add(UI.titulo("Sistema de Biblioteca Escolar"), BorderLayout.NORTH);
        panel.add(UI.formulario("Correo:", txtCorreo, "Contraseña:", txtClave), BorderLayout.CENTER);
        panel.add(btnIngresar, BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void ingresar() {
        try {
            Usuario u = controller.autenticar(txtCorreo.getText(), new String(txtClave.getPassword()));
            Estudiante estudiante = u.esBibliotecario() ? null : controller.estudianteDe(u);
            new MainView(u, estudiante).setVisible(true);
            dispose();
        } catch (Exception ex) {
            UI.error(this, ex);
            txtClave.setText("");
        }
    }
}
