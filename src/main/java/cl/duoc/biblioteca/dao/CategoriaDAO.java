package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private Connection cn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement("SELECT id, nombre FROM categorias ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
        }
        return lista;
    }

    public void insertar(String nombre) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("INSERT INTO categorias (nombre) VALUES (?)")) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("UPDATE categorias SET nombre = ? WHERE id = ?")) {
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM categorias WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
