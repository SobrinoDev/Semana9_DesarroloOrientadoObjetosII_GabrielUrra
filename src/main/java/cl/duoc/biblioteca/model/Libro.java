package cl.duoc.biblioteca.model;

public class Libro {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private int idCategoria;
    private String nombreCategoria;

    public Libro(int id, String titulo, String autor, String isbn, String editorial,
                 int stock, int idCategoria, String nombreCategoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    public boolean estaDisponible() {
        return stock > 0;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getIsbn() { return isbn; }
    public String getEditorial() { return editorial; }
    public int getStock() { return stock; }
    public int getIdCategoria() { return idCategoria; }
    public String getNombreCategoria() { return nombreCategoria; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setEditorial(String editorial) { this.editorial = editorial; }
    public void setStock(int stock) { this.stock = stock; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    @Override
    public String toString() {
        return titulo + " [stock: " + stock + "]";
    }
}
