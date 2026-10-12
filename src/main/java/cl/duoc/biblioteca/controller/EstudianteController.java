package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;

import java.sql.SQLException;
import java.util.List;

public class EstudianteController {

    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Estudiante> listar() throws SQLException {
        return estudianteDAO.listar();
    }

    /**
     * Inserta o actualiza al estudiante. Al crearlo con contraseña también se registra
     * como usuario del sistema con rol ESTUDIANTE, vinculado por el mismo RUT.
     */
    public void guardar(Estudiante e, String contrasena) throws SQLException {
        if (e.getNombre().isBlank() || e.getRut().isBlank() || e.getCurso().isBlank() || e.getCorreo().isBlank()) {
            throw new IllegalArgumentException("Todos los campos del estudiante son obligatorios.");
        }
        if (!e.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo no es válido.");
        }
        if (e.getId() == 0) {
            estudianteDAO.insertar(e);
            if (!contrasena.isBlank()) {
                usuarioDAO.insertar(new Usuario(0, e.getNombre(), e.getRut(), e.getCorreo(), Usuario.ROL_ESTUDIANTE), contrasena);
            }
        } else {
            estudianteDAO.actualizar(e);
            usuarioDAO.actualizarPorRut(e.getRut(), e.getNombre(), e.getCorreo());
        }
    }

    public void eliminar(Estudiante e) throws SQLException {
        estudianteDAO.eliminar(e.getId());
        usuarioDAO.eliminarPorRut(e.getRut());
    }
}
