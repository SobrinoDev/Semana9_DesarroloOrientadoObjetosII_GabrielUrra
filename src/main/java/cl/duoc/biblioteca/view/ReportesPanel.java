package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.controller.ReporteController;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReportesPanel extends JPanel {

    private static final String MAS_PRESTADOS = "Libros más prestados";
    private static final String HISTORIAL = "Historial por estudiante";
    private static final String EN_PRESTAMO = "Libros actualmente en préstamo";

    private final ReporteController controller;
    private final EstudianteController estudianteController;
    private final JComboBox<String> cbReporte = new JComboBox<>(new String[]{MAS_PRESTADOS, HISTORIAL, EN_PRESTAMO});
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final DefaultTableModel modelo = UI.modelo();
    private final JTable tabla = UI.tabla(modelo);
    private final JLabel lblResumen = new JLabel(" ");

    public ReportesPanel(ReporteController controller, EstudianteController estudianteController) {
        super(new BorderLayout(10, 10));
        this.controller = controller;
        this.estudianteController = estudianteController;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnGenerar = new JButton("Generar");
        btnGenerar.addActionListener(e -> generar());
        cbReporte.addActionListener(e -> cbEstudiante.setEnabled(HISTORIAL.equals(cbReporte.getSelectedItem())));
        cbEstudiante.setEnabled(false);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Reporte:"));
        filtros.add(cbReporte);
        filtros.add(new JLabel("Estudiante:"));
        filtros.add(cbEstudiante);
        filtros.add(btnGenerar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(UI.titulo("Reportes"), BorderLayout.NORTH);
        norte.add(filtros, BorderLayout.CENTER);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);
    }

    public void refrescar() {
        try {
            cbEstudiante.setModel(new DefaultComboBoxModel<>(estudianteController.listar().toArray(new Estudiante[0])));
        } catch (Exception ex) {
            UI.error(this, ex);
        }
        generar();
    }

    private void generar() {
        try {
            modelo.setRowCount(0);
            String reporte = (String) cbReporte.getSelectedItem();
            if (MAS_PRESTADOS.equals(reporte)) {
                modelo.setColumnIdentifiers(new String[]{"Título", "Autor", "Veces prestado"});
                List<Object[]> filas = controller.librosMasPrestados();
                filas.forEach(modelo::addRow);
                lblResumen.setText(filas.size() + " libros en el ranking");
            } else {
                List<Prestamo> lista;
                if (HISTORIAL.equals(reporte)) {
                    Estudiante e = (Estudiante) cbEstudiante.getSelectedItem();
                    if (e == null) return;
                    lista = controller.historialPorEstudiante(e.getId());
                } else {
                    lista = controller.librosEnPrestamo();
                }
                modelo.setColumnIdentifiers(new String[]{"ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Entrega", "Estado"});
                for (Prestamo p : lista) {
                    modelo.addRow(new Object[]{p.getId(), p.getNombreEstudiante(), p.getTituloLibro(),
                            UI.fecha(p.getFechaPrestamo()), UI.fecha(p.getFechaDevolucion()),
                            UI.fecha(p.getFechaEntrega()), p.getEstado()});
                }
                long atrasados = lista.stream().filter(p -> p.diasAtraso() > 0).count();
                lblResumen.setText(lista.size() + " préstamos, " + atrasados + " con atraso");
            }
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }
}
