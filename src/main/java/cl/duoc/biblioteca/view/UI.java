package cl.duoc.biblioteca.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/** Utilidades compartidas por las vistas. */
final class UI {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private UI() {
    }

    static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static JTable tabla(DefaultTableModel modelo) {
        JTable t = new JTable(modelo);
        t.setRowSorter(new TableRowSorter<>(modelo));
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        return t;
    }

    /** Índice en la lista de datos de la fila seleccionada (considera el ordenamiento), o -1. */
    static int filaSeleccionada(JTable t) {
        int fila = t.getSelectedRow();
        return fila < 0 ? -1 : t.convertRowIndexToModel(fila);
    }

    @SuppressWarnings("unchecked")
    static void filtrar(JTable t, String texto) {
        ((TableRowSorter<DefaultTableModel>) t.getRowSorter())
                .setRowFilter(texto.isBlank() ? null : RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
    }

    static void alEscribir(JTextField campo, Runnable accion) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { accion.run(); }
            public void removeUpdate(DocumentEvent e) { accion.run(); }
            public void changedUpdate(DocumentEvent e) { accion.run(); }
        });
    }

    /** Formulario de dos columnas: etiqueta, campo, etiqueta, campo... */
    static JPanel formulario(Object... etiquetasYCampos) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < etiquetasYCampos.length; i += 2) {
            g.gridy = i / 2;
            g.gridx = 0;
            g.weightx = 0;
            p.add(new JLabel((String) etiquetasYCampos[i]), g);
            g.gridx = 1;
            g.weightx = 1;
            p.add((Component) etiquetasYCampos[i + 1], g);
        }
        return p;
    }

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        return l;
    }

    static String fecha(LocalDate f) {
        return f == null ? "" : f.format(FECHA);
    }

    static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    static void error(Component padre, Throwable e) {
        String msg = e.getMessage();
        if (msg != null && msg.contains("foreign key constraint")) {
            msg = "No se puede eliminar: el registro tiene préstamos asociados.";
        } else if (msg != null && msg.contains("Duplicate entry")) {
            msg = "Ya existe un registro con ese dato único (RUT, correo o ISBN).";
        }
        JOptionPane.showMessageDialog(padre, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static boolean confirmar(Component padre, String mensaje) {
        return JOptionPane.showConfirmDialog(padre, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
