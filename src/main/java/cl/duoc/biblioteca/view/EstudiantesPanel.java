package cl.duoc.biblioteca.view;

import cl.duoc.biblioteca.controller.EstudianteController;
import cl.duoc.biblioteca.model.Estudiante;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EstudiantesPanel extends JPanel {

    private final EstudianteController controller;
    private final DefaultTableModel modelo = UI.modelo("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable tabla = UI.tabla(modelo);
    private final JTextField txtBuscar = new JTextField(25);
    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtRut = new JTextField(20);
    private final JTextField txtCurso = new JTextField(20);
    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtClave = new JPasswordField(20);
    private List<Estudiante> estudiantes = List.of();
    private Estudiante seleccionado;

    public EstudiantesPanel(EstudianteController controller) {
        super(new BorderLayout(10, 10));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel norte = new JPanel(new FlowLayout(FlowLayout.LEFT));
        norte.add(UI.titulo("Gestión de estudiantes"));
        norte.add(Box.createHorizontalStrut(20));
        norte.add(new JLabel("Buscar:"));
        norte.add(txtBuscar);
        UI.alEscribir(txtBuscar, () -> UI.filtrar(tabla, txtBuscar.getText()));

        JButton btnNuevo = new JButton("Nuevo");
        JButton btnGuardar = new JButton("Guardar");
        JButton btnEliminar = new JButton("Eliminar");
        btnNuevo.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnEliminar.addActionListener(e -> eliminar());
        JPanel botones = new JPanel(new GridLayout(1, 3, 6, 6));
        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnEliminar);

        JLabel ayuda = new JLabel("<html><small>La contraseña (opcional, solo al crear) registra al estudiante<br>"
                + "como usuario del sistema para que pueda ingresar.</small></html>");

        JPanel este = new JPanel(new BorderLayout(0, 10));
        este.add(UI.formulario("Nombre:", txtNombre, "RUT:", txtRut, "Curso:", txtCurso,
                "Correo:", txtCorreo, "Contraseña:", txtClave), BorderLayout.NORTH);
        este.add(ayuda, BorderLayout.CENTER);
        este.add(botones, BorderLayout.SOUTH);

        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(este, BorderLayout.EAST);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });
    }

    public void refrescar() {
        try {
            estudiantes = controller.listar();
            modelo.setRowCount(0);
            for (Estudiante e : estudiantes) {
                modelo.addRow(new Object[]{e.getId(), e.getNombre(), e.getRut(), e.getCurso(), e.getCorreo()});
            }
            limpiar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void cargarSeleccion() {
        int i = UI.filaSeleccionada(tabla);
        if (i < 0) return;
        seleccionado = estudiantes.get(i);
        txtNombre.setText(seleccionado.getNombre());
        txtRut.setText(seleccionado.getRut());
        txtCurso.setText(seleccionado.getCurso());
        txtCorreo.setText(seleccionado.getCorreo());
        txtRut.setEditable(false);   // el RUT vincula estudiante y usuario
        txtClave.setEnabled(false);
    }

    private void limpiar() {
        seleccionado = null;
        tabla.clearSelection();
        for (JTextField t : new JTextField[]{txtNombre, txtRut, txtCurso, txtCorreo, txtClave}) t.setText("");
        txtRut.setEditable(true);
        txtClave.setEnabled(true);
    }

    private void guardar() {
        Estudiante e = new Estudiante(seleccionado == null ? 0 : seleccionado.getId(), txtNombre.getText().trim(),
                txtRut.getText().trim(), txtCurso.getText().trim(), txtCorreo.getText().trim());
        try {
            controller.guardar(e, new String(txtClave.getPassword()));
            UI.info(this, seleccionado == null ? "Estudiante registrado." : "Estudiante actualizado.");
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }

    private void eliminar() {
        if (seleccionado == null) {
            UI.info(this, "Seleccione un estudiante de la tabla.");
            return;
        }
        if (!UI.confirmar(this, "¿Eliminar a " + seleccionado.getNombre() + "?")) return;
        try {
            controller.eliminar(seleccionado);
            refrescar();
        } catch (Exception ex) {
            UI.error(this, ex);
        }
    }
}
