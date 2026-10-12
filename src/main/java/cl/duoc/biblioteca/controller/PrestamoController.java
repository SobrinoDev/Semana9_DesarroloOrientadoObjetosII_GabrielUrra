package cl.duoc.biblioteca.controller;

import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Prestamo;

import javax.swing.SwingWorker;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Se usa una única instancia compartida por todas las pantallas, para que los
 * métodos synchronized protejan el stock frente a préstamos simultáneos.
 */
public class PrestamoController {

    public static final int DIAS_PRESTAMO = 7;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final LibroDAO libroDAO = new LibroDAO();

    public LocalDate calcularVencimiento(LocalDate desde) {
        return desde.plusDays(DIAS_PRESTAMO);
    }

    public List<Prestamo> listar(Integer idEstudiante, boolean incluirDevueltos) throws SQLException {
        return prestamoDAO.listar(idEstudiante, incluirDevueltos);
    }

    /**
     * Verifica disponibilidad, registra el préstamo (historial del estudiante) y descuenta el stock.
     * synchronized: solo un hilo a la vez puede "leer stock y descontarlo", evitando que dos
     * préstamos simultáneos tomen el último ejemplar (condición de carrera).
     */
    public synchronized int registrarPrestamo(int idEstudiante, int idLibro) throws SQLException {
        if (libroDAO.obtenerStock(idLibro) <= 0) {
            throw new IllegalStateException("El libro no tiene stock disponible.");
        }
        LocalDate hoy = LocalDate.now();
        return prestamoDAO.registrar(idEstudiante, idLibro, hoy, calcularVencimiento(hoy));
    }

    /**
     * Ejecuta registrarPrestamo en un hilo de fondo (SwingWorker) para no congelar la interfaz.
     * Los callbacks se ejecutan de vuelta en el hilo de Swing.
     */
    public void registrarPrestamoEnSegundoPlano(int idEstudiante, int idLibro,
                                                Consumer<Integer> alTerminar, Consumer<Throwable> alFallar) {
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Thread.sleep(1000); // pausa breve para evidenciar en la demo que la interfaz sigue respondiendo
                return registrarPrestamo(idEstudiante, idLibro);
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (ExecutionException e) {
                    alFallar.accept(e.getCause());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(e);
                }
            }
        }.execute();
    }

    /** Registra la devolución y repone el stock. Devuelve los días de atraso (0 si llegó a tiempo). */
    public synchronized long registrarDevolucion(Prestamo p) throws SQLException {
        if (p.isDevuelto()) {
            throw new IllegalStateException("Ese préstamo ya fue devuelto.");
        }
        prestamoDAO.devolver(p.getId(), LocalDate.now());
        return p.diasAtraso();
    }
}
