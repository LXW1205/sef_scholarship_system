package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnection {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        // Try to find the URL in common environment variables
        String envUrl = System.getenv("DB_URL");
        if (envUrl == null) envUrl = System.getenv("DATABASE_URL");
        
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        if (envUrl != null) {
            // Fix prefix if it's a standard Render/Heroku URL
            if (envUrl.startsWith("postgres://")) {
                envUrl = "jdbc:postgresql://" + envUrl.substring(11);
            } else if (envUrl.startsWith("postgresql://")) {
                envUrl = "jdbc:postgresql://" + envUrl.substring(13);
            } else if (!envUrl.startsWith("jdbc:postgresql://")) {
                envUrl = "jdbc:postgresql://" + envUrl;
            }

            URL = envUrl;
            USER = (envUser != null) ? envUser : ""; // Often embedded in URL
            PASSWORD = (envPassword != null) ? envPassword : "";
            System.out.println("[DB] Using environment variable URL: " + URL.split("@")[URL.split("@").length-1]); // Log host only for safety
        } else {
            Properties prop = new Properties();
            try (FileInputStream input = new FileInputStream("db.properties")) {
                prop.load(input);
                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.user");
                PASSWORD = prop.getProperty("db.password");
                System.out.println("[DB] Loaded credentials from db.properties.");
            } catch (IOException ex) {
                System.err.println("[DB] Error: No environment variables found AND no db.properties found.");
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        }
    }
}
