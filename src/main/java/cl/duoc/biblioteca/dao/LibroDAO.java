package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {

    private static final String SELECT =
            "SELECT l.*, c.nombre AS categoria FROM libros l JOIN categorias c ON c.id = l.id_categoria";

    private Connection cn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Libro> listar() throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(SELECT + " ORDER BY l.titulo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(map(rs));
        }
        return lista;
    }

    public int obtenerStock(int idLibro) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("SELECT stock FROM libros WHERE id = ?")) {
            ps.setInt(1, idLibro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public void insertar(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            bind(ps, l);
            ps.executeUpdate();
        }
    }

    public void actualizar(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            bind(ps, l);
            ps.setInt(7, l.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM libros WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, Libro l) throws SQLException {
        ps.setString(1, l.getTitulo());
        ps.setString(2, l.getAutor());
        ps.setString(3, l.getIsbn());
        ps.setString(4, l.getEditorial());
        ps.setInt(5, l.getStock());
        ps.setInt(6, l.getIdCategoria());
    }

    private static Libro map(ResultSet rs) throws SQLException {
        return new Libro(rs.getInt("id"), rs.getString("titulo"), rs.getString("autor"), rs.getString("isbn"),
                rs.getString("editorial"), rs.getInt("stock"), rs.getInt("id_categoria"), rs.getString("categoria"));
    }
}
