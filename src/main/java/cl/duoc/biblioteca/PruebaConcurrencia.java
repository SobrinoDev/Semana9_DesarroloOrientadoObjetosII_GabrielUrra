package cl.duoc.biblioteca;

import cl.duoc.biblioteca.controller.PrestamoController;
import cl.duoc.biblioteca.dao.EstudianteDAO;
import cl.duoc.biblioteca.dao.LibroDAO;
import cl.duoc.biblioteca.dao.PrestamoDAO;
import cl.duoc.biblioteca.model.Libro;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Prueba de control de stock: lanza más hilos que ejemplares disponibles, todos pidiendo
 * el mismo libro al mismo tiempo. Debe prestarse exactamente el stock que había y nunca quedar negativo.
 * Al final deshace los préstamos de prueba. Ejecutar con la base de datos levantada.
 */
public class PruebaConcurrencia {

    public static void main(String[] args) throws Exception {
        LibroDAO libroDAO = new LibroDAO();
        Libro libro = libroDAO.listar().stream().filter(Libro::estaDisponible).findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay libros con stock"));
        int idEstudiante = new EstudianteDAO().listar().get(0).getId();
        int stockInicial = libro.getStock();
        int hilos = stockInicial + 5;

        PrestamoController controller = new PrestamoController(); // instancia compartida por todos los hilos
        AtomicInteger rechazados = new AtomicInteger();
        List<Integer> creados = new CopyOnWriteArrayList<>();
        CountDownLatch largada = new CountDownLatch(1);
        Thread[] ts = new Thread[hilos];

        System.out.printf("Libro \"%s\" con stock %d. Lanzando %d préstamos simultáneos...%n", libro.getTitulo(), stockInicial, hilos);
        for (int i = 0; i < hilos; i++) {
            ts[i] = new Thread(() -> {
                try {
                    largada.await();
                    creados.add(controller.registrarPrestamo(idEstudiante, libro.getId()));
                } catch (Exception e) {
                    rechazados.incrementAndGet();
                }
            }, "prestamo-" + i);
            ts[i].start();
        }
        largada.countDown(); // todos parten a la vez
        for (Thread t : ts) t.join();

        int stockFinal = libroDAO.obtenerStock(libro.getId());
        System.out.printf("Aprobados: %d | Rechazados: %d | Stock final: %d%n", creados.size(), rechazados.get(), stockFinal);
        boolean ok = creados.size() == stockInicial && stockFinal == 0 && rechazados.get() == 5;

        // Limpieza: se borran los préstamos de prueba y se restaura el stock
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        for (int id : creados) prestamoDAO.eliminar(id);
        libro.setStock(stockInicial);
        libroDAO.actualizar(libro);

        System.out.println(ok ? "OK: el stock se mantuvo consistente." : "FALLO: inconsistencia de stock.");
        System.exit(ok ? 0 : 1);
    }
}
