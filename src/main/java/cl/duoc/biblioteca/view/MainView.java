package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;

import javax.swing.*;
import java.awt.*;

/** Ventana principal con panel de navegación lateral. Las opciones dependen del rol. */
public class MainView extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel contenido = new JPanel(cards);
    private final JPanel navegacion = new JPanel(new GridLayout(0, 1, 0, 6));

    /** estudiante es null cuando ingresa un bibliotecario. */
    public MainView(Usuario usuario, Estudiante estudiante) {
        super("Biblioteca Escolar - " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        LibroController libroController = new LibroController();
        EstudianteController estudianteController = new EstudianteController();
        PrestamoController prestamoController = new PrestamoController(); // instancia única compartida
        boolean bibliotecario = usuario.esBibliotecario();

        LibrosPanel libros = new LibrosPanel(libroController, bibliotecario);
        PrestamosPanel prestamos = new PrestamosPanel(prestamoController, libroController, estudianteController, estudiante);
        agregar("Libros", libros, libros::refrescar);
        if (bibliotecario) {
            EstudiantesPanel estudiantes = new EstudiantesPanel(estudianteController);
            agregar("Estudiantes", estudiantes, estudiantes::refrescar);
        }
        agregar(bibliotecario ? "Préstamos y devoluciones" : "Mis préstamos", prestamos, prestamos::refrescar);
        if (bibliotecario) {
            ReportesPanel reportes = new ReportesPanel(new ReporteController(), estudianteController);
            agregar("Reportes", reportes, reportes::refrescar);
        }

        JButton salir = new JButton("Cerrar sesión");
        salir.addActionListener(e -> {
            dispose();
            new LoginView().setVisible(true);
        });

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        lateral.add(navegacion, BorderLayout.NORTH);
        lateral.add(salir, BorderLayout.SOUTH);

        JLabel estado = new JLabel(" Sesión: " + usuario.getNombre() + " | " + usuario.getCorreo() + " | Rol: " + usuario.getRol());
        estado.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        add(lateral, BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        libros.refrescar();
    }

    private void agregar(String nombre, JPanel panel, Runnable refrescar) {
        contenido.add(panel, nombre);
        JButton boton = new JButton(nombre);
        boton.addActionListener(e -> {
            refrescar.run();
            cards.show(contenido, nombre);
        });
        navegacion.add(boton);
    }
}
