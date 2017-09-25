
import java.sql.*;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that manages connections to the database. It also has a utility
 * method that close connections, statements and resultsets
 */
public class ConnectionManager {

    private static final String PROPS_FILENAME = "/connection.properties";
    private static String dbUser;
    private static String dbPassword;
    private static String dbURL;

    static {
        readDatabaseProperties();

        initDBDriver();
    }

    private static void readDatabaseProperties() {
        try (
                // Retrieve properties from connection.properties via the CLASSPATH
                // WEB-INF/classes is on the CLASSPATH
                InputStream is = ConnectionManager.class.getResourceAsStream(PROPS_FILENAME);) {
            Properties props = new Properties();
            props.load(is);

            // load database connection details
            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port");
            String dbName = props.getProperty("db.name");
            dbUser = props.getProperty("db.user");

            // grab environment variable to check if we are on production environment
            String osName = System.getProperty("os.name");
            if (osName.equals("Linux")) {
                // in production environment, use aws.db.password
                dbPassword = props.getProperty("aws.db.password");
            } else {
                // in local environment, use db.password
                dbPassword = props.getProperty("db.password");
            }
            dbURL = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        } catch (Exception ex) {
            // unable to load properties file
            String message = "Unable to load '" + PROPS_FILENAME + "'.";

            System.out.println(message);
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        }
    }

    private static void initDBDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // unable to load properties file
            String message = "Unable to find JDBC driver for MySQL.";

            System.out.println(message);
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        }
    }

    /**
     * Gets a connection to the database
     *
     * @return the connection
     * @throws SQLException if an error occurs when connecting
     */
    public static Connection getConnection() throws SQLException {
        String message = "dbURL: " + dbURL
                + "  , dbUser: " + dbUser
                + "  , dbPassword: " + dbPassword;
        Logger.getLogger(ConnectionManager.class.getName()).log(Level.INFO, message);

        return DriverManager.getConnection(dbURL, dbUser, dbPassword);

    }

}
