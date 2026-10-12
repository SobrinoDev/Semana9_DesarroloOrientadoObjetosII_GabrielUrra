package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.UsuarioDAO;
import cl.duoc.biblioteca.model.Estudiante;
import cl.duoc.biblioteca.model.Usuario;

import java.sql.SQLException;

public class LoginController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        if (correo.isBlank() || contrasena.isBlank()) {
            throw new IllegalArgumentException("Ingrese correo y contraseña.");
        }
        Usuario u = usuarioDAO.autenticar(correo.trim(), contrasena);
        if (u == null) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }
        return u;
    }

    /** Un usuario con rol estudiante se vincula a su ficha de estudiante por el RUT. */
    public Estudiante estudianteDe(Usuario u) throws SQLException {
        Estudiante e = estudianteDAO.buscarPorRut(u.getRut());
        if (e == null) {
            throw new IllegalStateException("El usuario no está vinculado a ningún estudiante.");
        }
        return e;
    }
}
