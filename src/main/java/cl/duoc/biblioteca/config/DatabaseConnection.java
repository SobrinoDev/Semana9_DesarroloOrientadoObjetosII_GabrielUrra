package cl.duoc.biblioteca.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patrón Singleton: existe una única instancia que administra la única conexión
 * JDBC de la aplicación. Los datos de conexión se pueden cambiar en un archivo
 * db.properties junto al ejecutable, sin recompilar.
 */
public final class DatabaseConnection {

    private static DatabaseConnection instance;

    private final String url;
    private final String usuario;
    private final String clave;
    private Connection connection;

    private DatabaseConnection() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException ignored) {
            // sin archivo: se usan los valores por defecto
        }
        url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/biblioteca_escolar?serverTimezone=America/Santiago");
        usuario = p.getProperty("db.user", "root");
        clave = p.getProperty("db.password", "");
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Devuelve la conexión, abriéndola de nuevo si se cerró o se perdió. */
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || !connection.isValid(2)) {
            connection = DriverManager.getConnection(url, usuario, clave);
        }
        return connection;
    }
}
