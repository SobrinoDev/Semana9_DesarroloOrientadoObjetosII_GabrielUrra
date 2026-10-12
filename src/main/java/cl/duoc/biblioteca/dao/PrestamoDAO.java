package cl.duoc.biblioteca.dao;

import cl.duoc.biblioteca.config.DatabaseConnection;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private static final String SELECT =
            "SELECT p.*, e.nombre AS estudiante, l.titulo AS libro FROM prestamos p "
            + "JOIN estudiantes e ON e.id = p.id_estudiante JOIN libros l ON l.id = p.id_libro";

    private Connection cn() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Lista préstamos. idEstudiante = null para todos; incluirDevueltos = false para
     * ver solo los que están actualmente prestados.
     */
    public List<Prestamo> listar(Integer idEstudiante, boolean incluirDevueltos) throws SQLException {
        String sql = SELECT + " WHERE (? IS NULL OR p.id_estudiante = ?) AND (? OR p.devuelto = 0)"
                + " ORDER BY p.fecha_prestamo DESC, p.id DESC";
        List<Prestamo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(sql)) {
            ps.setObject(1, idEstudiante);
            ps.setObject(2, idEstudiante);
            ps.setBoolean(3, incluirDevueltos);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(map(rs));
            }
        }
        return lista;
    }

    /** Ranking de libros más prestados: {título, autor, veces}. */
    public List<Object[]> librosMasPrestados() throws SQLException {
        String sql = "SELECT l.titulo, l.autor, COUNT(*) AS veces FROM prestamos p JOIN libros l ON l.id = p.id_libro "
                + "GROUP BY l.id, l.titulo, l.autor ORDER BY veces DESC, l.titulo LIMIT 10";
        List<Object[]> filas = new ArrayList<>();
        try (PreparedStatement ps = cn().prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) filas.add(new Object[]{rs.getString(1), rs.getString(2), rs.getInt(3)});
        }
        return filas;
    }

    /**
     * Registra el préstamo y descuenta el stock en una sola transacción.
     * El UPDATE con "stock > 0" es una segunda barrera en la propia base de datos:
     * si otro proceso dejó el stock en 0, no se descuenta y se revierte todo.
     */
    public int registrar(int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate vencimiento) throws SQLException {
        Connection c = cn();
        // ponytail: conexión única compartida; se bloquea mientras dura la transacción para que otro hilo no se mezcle en ella
        synchronized (c) {
            c.setAutoCommit(false);
            try (PreparedStatement stock = c.prepareStatement("UPDATE libros SET stock = stock - 1 WHERE id = ? AND stock > 0");
                 PreparedStatement ins = c.prepareStatement(
                         "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) VALUES (?, ?, ?, ?, 0)",
                         Statement.RETURN_GENERATED_KEYS)) {
                stock.setInt(1, idLibro);
                if (stock.executeUpdate() == 0) {
                    throw new SQLException("El libro no tiene stock disponible.");
                }
                ins.setInt(1, idEstudiante);
                ins.setInt(2, idLibro);
                ins.setDate(3, Date.valueOf(fechaPrestamo));
                ins.setDate(4, Date.valueOf(vencimiento));
                ins.executeUpdate();
                int id;
                try (ResultSet keys = ins.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Marca la devolución con su fecha real y repone el stock, en una transacción. */
    public void devolver(int idPrestamo, LocalDate fechaEntrega) throws SQLException {
        Connection c = cn();
        synchronized (c) {
            c.setAutoCommit(false);
            try (PreparedStatement dev = c.prepareStatement(
                         "UPDATE prestamos SET devuelto = 1, fecha_entrega = ? WHERE id = ? AND devuelto = 0");
                 PreparedStatement stock = c.prepareStatement(
                         "UPDATE libros SET stock = stock + 1 WHERE id = (SELECT id_libro FROM prestamos WHERE id = ?)")) {
                dev.setDate(1, Date.valueOf(fechaEntrega));
                dev.setInt(2, idPrestamo);
                if (dev.executeUpdate() == 0) {
                    throw new SQLException("El préstamo ya estaba devuelto.");
                }
                stock.setInt(1, idPrestamo);
                stock.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void eliminar(int idPrestamo) throws SQLException {
        try (PreparedStatement ps = cn().prepareStatement("DELETE FROM prestamos WHERE id = ?")) {
            ps.setInt(1, idPrestamo);
            ps.executeUpdate();
        }
    }

    private static Prestamo map(ResultSet rs) throws SQLException {
        Date entrega = rs.getDate("fecha_entrega");
        return new Prestamo(rs.getInt("id"), rs.getInt("id_estudiante"), rs.getInt("id_libro"),
                rs.getDate("fecha_prestamo").toLocalDate(), rs.getDate("fecha_devolucion").toLocalDate(),
                rs.getBoolean("devuelto"), entrega == null ? null : entrega.toLocalDate(),
                rs.getString("estudiante"), rs.getString("libro"));
    }
}
