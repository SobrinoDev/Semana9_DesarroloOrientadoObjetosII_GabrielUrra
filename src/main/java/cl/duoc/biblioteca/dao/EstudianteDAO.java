package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Estudiante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    private Connection cn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Estudiante> listar() throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement("SELECT * FROM estudiantes ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public Estudiante buscarPorRut(String rut) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT * FROM estudiantes WHERE rut = ?")) {
            ps.setString(1, rut);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void insertar(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        }
    }

    public void actualizar(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, curso = ?, correo = ? WHERE id = ?";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getCurso());
            ps.setString(3, e.getCorreo());
            ps.setInt(4, e.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM estudiantes WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static Estudiante map(ResultSet rs) throws SQLException {
        return new Estudiante(rs.getInt("id"), rs.getString("nombre"), rs.getString("rut"),
                rs.getString("curso"), rs.getString("correo"));
    }
}
