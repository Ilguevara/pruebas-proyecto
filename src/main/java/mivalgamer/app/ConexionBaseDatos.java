package mivalgamer.app;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexionBaseDatos {
    private static final Logger LOGGER = Logger.getLogger(ConexionBaseDatos.class.getName());
    private static final String DB_URL = "jdbc:mysql://bkgshsddffwp1gdiqqcl-mysql.services.clever-cloud.com/bkgshsddffwp1gdiqqcl";
    private static final String DB_USER = "uzjpyykc41cm3273";
    private static final String DB_PASS = "oq4aVRScMPmmRQS6SciH";

    private Connection connection;

    public Connection conectar() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            return connection;
        } catch (ClassNotFoundException | SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error de conexión", ex);
            return null;
        }
    }

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al cerrar conexión", ex);
        }
    }
}