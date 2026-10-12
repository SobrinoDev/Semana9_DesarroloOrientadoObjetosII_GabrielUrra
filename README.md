# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio en Java 17 (Swing/JFrame) con MySQL (JDBC) para gestionar libros, estudiantes,
préstamos, devoluciones y reportes de una biblioteca escolar.

## Requisitos
- JDK 17 o superior
- MySQL 8
- IntelliJ IDEA (el proyecto es Maven; el driver MySQL Connector/J se descarga solo)

## Puesta en marcha
1. En MySQL ejecutar, en este orden:
   - `sql/Script_crea_tablas_biblioteca.sql`
   - `sql/Script_poblado_tablas_biblioteca.sql`
2. Configurar la conexión: copiar `db.properties.example` como `db.properties` (en la carpeta del
   proyecto o junto al `.jar`) y poner el usuario/clave de MySQL. Sin ese archivo se usa `root` sin clave.
3. Ejecutar `cl.duoc.biblioteca.Main` desde IntelliJ, o el ejecutable:
   ```
   java -jar BibliotecaEscolar.jar
   ```
   (Se genera con `mvn package` en `target/BibliotecaEscolar.jar`.)

## Usuarios de prueba
| Rol | Correo | Contraseña |
|---|---|---|
| Bibliotecario | admin@biblioteca.cl | admin123 |
| Estudiante | vmunoz@colegio.cl | est123 |

## Funcionalidades
- **Autenticación** por correo y contraseña (guardada como hash SHA-256). El rol define el menú:
  - Bibliotecario: libros (CRUD), estudiantes (CRUD), préstamos/devoluciones y reportes.
  - Estudiante: consulta del catálogo y sus propios préstamos/devoluciones.
- **Libros**: registrar, modificar, eliminar, buscar; cada libro tiene categoría, autor, editorial, ISBN y stock.
- **Estudiantes**: CRUD; al crearlo con contraseña queda también como usuario (vinculado por RUT).
- **Préstamos**: verifica stock, calcula el vencimiento (7 días), descuenta stock; la devolución
  repone stock, guarda la fecha real y deja constancia del atraso (se marca en rojo).
- **Reportes**: libros más prestados, historial por estudiante, libros actualmente en préstamo.

## Arquitectura y decisiones de diseño
```
cl.duoc.biblioteca
├── config      DatabaseConnection (Singleton)
├── model       Usuario, Estudiante, Libro, Categoria, Prestamo
├── dao         UsuarioDAO, EstudianteDAO, LibroDAO, CategoriaDAO, PrestamoDAO
├── controller  LoginController, LibroController, EstudianteController, PrestamoController, ReporteController
└── view        LoginView, MainView (JFrame) y paneles por módulo
```
- **MVC**: las vistas solo muestran datos y capturan eventos; los controladores validan y aplican las
  reglas de negocio; los modelos representan las entidades.
- **DAO**: toda la SQL está en las clases DAO (con `PreparedStatement`, que evita inyección SQL).
  El resto del sistema no sabe que existe MySQL.
- **Singleton**: `DatabaseConnection.getInstance()` entrega siempre la misma instancia, que mantiene una
  única conexión y la reabre si se pierde.
- **Hilos**: el préstamo (verificar stock, registrar préstamo/historial y descontar stock) se ejecuta
  en un `SwingWorker`, un hilo de fondo; mientras tanto la ventana sigue respondiendo y se ve una barra
  de progreso.
- **Sincronización**: `PrestamoController.registrarPrestamo` y `registrarDevolucion` son `synchronized`
  y hay una sola instancia del controlador, así que dos préstamos simultáneos no pueden tomar el mismo
  último ejemplar. Además el SQL descuenta con `WHERE stock > 0` dentro de una transacción
  (commit/rollback), como segunda barrera en la base de datos.

## Prueba de concurrencia
Ejecutar `cl.duoc.biblioteca.PruebaConcurrencia`: lanza más hilos que ejemplares disponibles contra el mismo
libro. Debe aprobar exactamente el stock que había, rechazar el resto y dejar el stock en 0 (luego
deshace los préstamos de prueba).
