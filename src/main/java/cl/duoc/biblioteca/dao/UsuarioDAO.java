package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection cn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /** Devuelve el usuario si correo y contraseña coinciden; la contraseña se compara como hash SHA-256. */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, rol FROM usuarios WHERE correo = ? AND contrasena = SHA2(?, 256)";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement("SELECT id, nombre, rut, correo, rol FROM usuarios ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public void insertar(Usuario u, String contrasena) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, contrasena, rol) VALUES (?, ?, ?, SHA2(?, 256), ?)";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, contrasena);
            ps.setString(5, u.getRol());
            ps.executeUpdate();
        }
    }

    /** Actualiza nombre y correo del usuario con ese RUT (mantiene sincronizado al estudiante vinculado). */
    public void actualizarPorRut(String rut, String nombre, String correo) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("UPDATE usuarios SET nombre = ?, correo = ? WHERE rut = ?")) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, rut);
            ps.executeUpdate();
        }
    }

    public void eliminarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM usuarios WHERE rut = ? AND rol = ?")) {
            ps.setString(1, rut);
            ps.setString(2, Usuario.ROL_ESTUDIANTE);
            ps.executeUpdate();
        }
    }

    private static Usuario map(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("correo"), rs.getString("rol"));
    }
}
