package managers.dbLogic;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class ConnectionFactory {
    private static Properties dbProperties = null;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (FileInputStream fis = new FileInputStream("src/resources/db.cfg")) {
            dbProperties = new Properties();
            dbProperties.load(fis);
        } catch (IOException e) {
            Logger.getLogger(ConnectionFactory.class.getName()).severe("Failed to load database configuration: " + e.getMessage());
            throw new RuntimeException("Failed to load database properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = dbProperties.getProperty("db.url");
        String user = dbProperties.getProperty("db.user");
        String password = dbProperties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }

}
