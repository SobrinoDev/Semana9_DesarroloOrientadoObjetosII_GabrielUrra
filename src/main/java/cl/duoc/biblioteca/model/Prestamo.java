package cl.duoc.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;   // vencimiento
    private boolean devuelto;
    private LocalDate fechaEntrega;      // devolución real
    private String nombreEstudiante;
    private String tituloLibro;

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion,
                    boolean devuelto, LocalDate fechaEntrega, String nombreEstudiante, String tituloLibro) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
        this.fechaEntrega = fechaEntrega;
        this.nombreEstudiante = nombreEstudiante;
        this.tituloLibro = tituloLibro;
    }

    /** Días de atraso: hasta hoy si sigue prestado, o hasta la entrega si ya se devolvió. */
    public long diasAtraso() {
        LocalDate referencia = devuelto ? fechaEntrega : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(fechaDevolucion, referencia));
    }

    public String getEstado() {
        long atraso = diasAtraso();
        if (devuelto) {
            return atraso > 0 ? "Devuelto con atraso (" + atraso + " días)" : "Devuelto";
        }
        return atraso > 0 ? "ATRASADO (" + atraso + " días)" : "En préstamo";
    }

    public int getId() { return id; }
    public int getIdEstudiante() { return idEstudiante; }
    public int getIdLibro() { return idLibro; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public boolean isDevuelto() { return devuelto; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public String getNombreEstudiante() { return nombreEstudiante; }
    public String getTituloLibro() { return tituloLibro; }
}
