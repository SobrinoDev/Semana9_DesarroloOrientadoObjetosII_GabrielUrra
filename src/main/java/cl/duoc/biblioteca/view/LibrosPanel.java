package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.LibroController;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Gestión de libros (bibliotecario) o consulta del catálogo (estudiante). */
public class LibrosPanel extends JPanel {

    private final LibroController controller;
    private final DefaultTableModel modelo = UI.modelo("ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(25);
    private final JTextField txtTitulo = new JTextField(20);
    private final JTextField txtAutor = new JTextField(20);
    private final JTextField txtIsbn = new JTextField(20);
    private final JTextField txtEditorial = new JTextField(20);
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
    private final JComboBox<Categoria> cbCategoria = new JComboBox<>();
    private List<Libro> libros = List.of();
    private Libro seleccionado;

    public LibrosPanel(LibroController controller, boolean editable) {
        super(new BorderLayout(10, 10));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(UI.titulo(editable ? "Gestión de libros" : "Catálogo de libros"));
        norte.add(Box.createHorizontalStrut(20));
        norte.add(new JLabel("Buscar:"));
        norte.add(txtBuscar);
        UI.alEscribir(txtBuscar, () -> UI.filtrar(tabla, txtBuscar.getText()));

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        if (editable) {
            JButton btnGuardar = new JButton("Guardar");
            JButton btnNuevo = new JButton("Nuevo");
            JButton btnEliminar = new JButton("Eliminar");
            JButton btnCategoria = new JButton("+ Categoría");
            btnGuardar.addActionListener(e -> guardar());
            btnNuevo.addActionListener(e -> limpiar());
            btnEliminar.addActionListener(e -> eliminar());
            btnCategoria.addActionListener(e -> nuevaCategoria());

            JPanel botones = new JPanel(new GridLayout(2, 2, 6, 6));
            botones.add(btnNuevo);
            botones.add(btnGuardar);
            botones.add(btnEliminar);
            botones.add(btnCategoria);

            JPanel este = new JPanel(new BorderLayout(0, 10));
            este.add(UI.formulario("Título:", txtTitulo, "Autor:", txtAutor, "ISBN:", txtIsbn,
                    "Editorial:", txtEditorial, "Stock:", spStock, "Categoría:", cbCategoria), BorderLayout.NORTH);
            este.add(botones, BorderLayout.SOUTH);
            add(este, BorderLayout.EAST);

            tabla.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) cargarSeleccion();
            });
        }
    }

    public void refrescar() {
        try {
            libros = controller.listar();
            modelo.setRowCount(0);
            for (Libro l : libros) {
                modelo.addRow(new Object[]{l.getId(), l.getTitulo(), l.getAutor(), l.getIsbn(),
                        l.getEditorial(), l.getStock(), l.getNombreCategoria()});
            }
            cbCategoria.setModel(new DefaultComboBoxModel<>(controller.listarCategorias().toArray(new Categoria[0])));
            limpiar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void cargarSeleccion() {
        int i = UI.filaSeleccionada(tabla);
        if (i < 0) return;
        seleccionado = libros.get(i);
        txtTitulo.setText(seleccionado.getTitulo());
        txtAutor.setText(seleccionado.getAutor());
        txtIsbn.setText(seleccionado.getIsbn());
        txtEditorial.setText(seleccionado.getEditorial());
        spStock.setValue(seleccionado.getStock());
        for (int c = 0; c < cbCategoria.getItemCount(); c++) {
            if (cbCategoria.getItemAt(c).getId() == seleccionado.getIdCategoria()) cbCategoria.setSelectedIndex(c);
        }
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        spStock.setValue(1);
    }

    private void guardar() {
        Categoria cat = (Categoria) cbCategoria.getSelectedItem();
        if (cat == null) {
            UI.info(this, "Primero cree una categoría.");
            return;
        }
        Libro l = new Libro(seleccionado == null ? 0 : seleccionado.getId(), txtTitulo.getText().trim(),
                txtAutor.getText().trim(), txtIsbn.getText().trim(), txtEditorial.getText().trim(),
                (Integer) spStock.getValue(), cat.getId(), cat.getNombre());
        try {
            controller.guardar(l);
            UI.info(this, seleccionado == null ? "Libro registrado." : "Libro actualizado.");
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            UI.info(this, "Seleccione un libro de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Eliminar \"" + seleccionado.getTitulo() + "\"?")) return;
        try {
            controller.eliminar(seleccionado);
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void nuevaCategoria() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre de la nueva categoría:");
        if (nombre == null) return;
        try {
            controller.crearCategoria(nombre);
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }
}
