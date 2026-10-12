package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Prestamo;

import java.sql.SQLException;
import java.util.List;

public class ReporteController {

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    public List<Object[]> librosMasPrestados() throws SQLException {
        return prestamoDAO.librosMasPrestados();
    }

    public List<Prestamo> historialPorEstudiante(int idEstudiante) throws SQLException {
        return prestamoDAO.listar(idEstudiante, true);
    }

    public List<Prestamo> librosEnPrestamo() throws SQLException {
        return prestamoDAO.listar(null, false);
    }
}
