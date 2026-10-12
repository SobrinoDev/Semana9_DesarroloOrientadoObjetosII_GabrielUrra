package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.CategoriaDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.model.Categoria;
import cl.duoc.biblioteca.model.Libro;

import java.sql.SQLException;
import java.util.List;

public class LibroController {

    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    public List<Libro> listar() throws SQLException {
        return libroDAO.listar();
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listar();
    }

    public void crearCategoria(String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        categoriaDAO.insertar(nombre.trim());
    }

    /** Inserta si el libro es nuevo (id 0) o actualiza si ya existe. */
    public void guardar(Libro l) throws SQLException {
        if (l.getTitulo().isBlank() || l.getAutor().isBlank() || l.getIsbn().isBlank() || l.getEditorial().isBlank()) {
            throw new IllegalArgumentException("Todos los campos del libro son obligatorios.");
        }
        if (l.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (l.getId() == 0) {
            libroDAO.insertar(l);
        } else {
            libroDAO.actualizar(l);
        }
    }

    public void eliminar(Libro l) throws SQLException {
        libroDAO.eliminar(l.getId());
    }
}
