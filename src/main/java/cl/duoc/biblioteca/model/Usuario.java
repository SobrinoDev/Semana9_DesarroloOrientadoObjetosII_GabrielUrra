package cl.duoc.biblioteca.model;

public class Usuario {

    public static final String ROL_BIBLIOTECARIO = "BIBLIOTECARIO";
    public static final String ROL_ESTUDIANTE = "ESTUDIANTE";

    private int id;
    private String nombre;
    private String rut;
    private String correo;
    private String rol;

    public Usuario(int id, String nombre, String rut, String correo, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
        this.rol = rol;
    }

    public boolean esBibliotecario() {
        return ROL_BIBLIOTECARIO.equalsIgnoreCase(rol);
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRut() { return rut; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setRol(String rol) { this.rol = rol; }
}
