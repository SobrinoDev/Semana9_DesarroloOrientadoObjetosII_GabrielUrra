package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Libro;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Registro de préstamos y devoluciones. Si estudianteSesion no es null, el panel
 * queda limitado a los préstamos de ese estudiante.
 */
public class PrestamosPanel extends JPanel {

    private final PrestamoController prestamoController;
    private final LibroController libroController;
    private final EstudianteController estudianteController;
    private final Estudiante estudianteSesion;

    private final DefaultTableModel modelo = UI.modelo("ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Entrega", "Estado");
    private final JTable tabla = UI.tabla(modelo);
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<Libro> cbLibro = new JComboBox<>();
    private final JCheckBox chkDevueltos = new JCheckBox("Mostrar devueltos");
    private final JButton btnPrestar = new JButton("Registrar préstamo");
    private final JProgressBar progreso = new JProgressBar();
    private List<Prestamo> prestamos = List.of();

    public PrestamosPanel(PrestamoController prestamoController, LibroController libroController,
                          EstudianteController estudianteController, Estudiante estudianteSesion) {
        super(new BorderLayout(10, 10));
        this.prestamoController = prestamoController;
        this.libroController = libroController;
        this.estudianteController = estudianteController;
        this.estudianteSesion = estudianteSesion;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        LocalDate vence = prestamoController.calcularVencimiento(LocalDate.now());
        JLabel lblVence = new JLabel("Vencimiento automático: " + UI.fecha(vence)
                + " (" + PrestamoController.DIAS_PRESTAMO + " días)");

        progreso.setIndeterminate(true);
        progreso.setString("Procesando préstamo en segundo plano...");
        progreso.setStringPainted(true);
        progreso.setVisible(false);

        btnPrestar.addActionListener(e -> prestar());
        JButton btnDevolver = new JButton("Registrar devolución del seleccionado");
        btnDevolver.addActionListener(e -> devolver());
        chkDevueltos.addActionListener(e -> cargarPrestamos());

        JPanel form = UI.formulario("Estudiante:", cbEstudiante, "Libro:", cbLibro, "", lblVence);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(btnPrestar);
        acciones.add(progreso);

        JPanel norte = new JPanel(new BorderLayout(0, 6));
        norte.add(UI.titulo(estudianteSesion == null ? "Préstamos y devoluciones" : "Mis préstamos"), BorderLayout.NORTH);
        norte.add(form, BorderLayout.CENTER);
        norte.add(acciones, BorderLayout.SOUTH);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sur.add(btnDevolver);
        sur.add(chkDevueltos);

        tabla.getColumnModel().getColumn(6).setCellRenderer(new EstadoRenderer());

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(sur, BorderLayout.SOUTH);
    }

    public void refrescar() {
        try {
            List<Estudiante> lista = estudianteSesion == null ? estudianteController.listar() : List.of(estudianteSesion);
            cbEstudiante.setModel(new DefaultComboBoxModel<>(lista.toArray(new Estudiante[0])));
            cbEstudiante.setEnabled(estudianteSesion == null);
            cbLibro.setModel(new DefaultComboBoxModel<>(libroController.listar().stream()
                    .filter(Libro::estaDisponible).toArray(Libro[]::new)));
        } catch (Exception ex) {
            UI.error(this, ex);
        }
        cargarPrestamos();
    }

    private void cargarPrestamos() {
        try {
            Integer id = estudianteSesion == null ? null : estudianteSesion.getId();
            prestamos = prestamoController.listar(id, chkDevueltos.isSelected());
            modelo.setRowCount(0);
            for (Prestamo p : prestamos) {
                modelo.addRow(new Object[]{p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                        UI.fecha(p.getFechaPrestamo()), UI.fecha(p.getFechaDevolucion()),
                        UI.fecha(p.getFechaEntrega()), p.getEstado()});
            }
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void prestar() {
        Estudiante est = (Estudiante) cbEstudiante.getSelectedItem();
        Libro libro = (Libro) cbLibro.getSelectedItem();
        if (est == null || libro == null) {
            UI.info(this, "Seleccione un estudiante y un libro con stock disponible.");
            return;
        }
        btnPrestar.setEnabled(false);
        progreso.setVisible(true);
        // El préstamo corre en un hilo aparte: la ventana se puede seguir usando mientras tanto.
        prestamoController.registrarPrestamoEnSegundoPlano(est.getId(), libro.getId(),
                id -> {
                    terminarProceso();
                    UI.info(this, "Préstamo N° " + id + " registrado.\n" + libro.getTitulo() + " → " + est.getNombre()
                            + "\nDevolver antes del " + UI.fecha(prestamoController.calcularVencimiento(LocalDate.now())));
                },
                error -> {
                    terminarProceso();
                    UI.error(this, error);
                });
    }

    private void terminarProceso() {
        btnPrestar.setEnabled(true);
        progreso.setVisible(false);
        refrescar();
    }

    private void devolver() {
        int i = UI.filaSeleccionada(tabla);
        if (i < 0) {
            UI.info(this, "Seleccione un préstamo de la tabla.");
            return;
        }
        Prestamo p = prestamos.get(i);
        try {
            long atraso = prestamoController.registrarDevolucion(p);
            UI.info(this, atraso > 0
                    ? "Devolución registrada CON ATRASO de " + atraso + " días. Queda constancia en el historial."
                    : "Devolución registrada a tiempo.");
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    /** Pinta en rojo los préstamos con atraso. */
    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel, boolean foco, int fila, int col) {
            Component c = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
            if (!sel) c.setForeground(String.valueOf(valor).contains("atraso") || String.valueOf(valor).startsWith("ATRASADO")
                    ? new Color(190, 30, 30) : t.getForeground());
            return c;
        }
    }
}
