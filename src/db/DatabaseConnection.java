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
        // Prefer environment variables (used on Render/cloud), fall back to db.properties (used locally)
        String envUrl      = System.getenv("DB_URL");
        String envUser     = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        if (envUrl != null && envUser != null && envPassword != null) {
            URL      = envUrl;
            USER     = envUser;
            PASSWORD = envPassword;
            System.out.println("[DB] Loaded credentials from environment variables.");
        } else {
            Properties prop = new Properties();
            try (FileInputStream input = new FileInputStream("db.properties")) {
                prop.load(input);
                URL      = prop.getProperty("db.url");
                USER     = prop.getProperty("db.user");
                PASSWORD = prop.getProperty("db.password");
                System.out.println("[DB] Loaded credentials from db.properties.");
            } catch (IOException ex) {
                System.err.println("[DB] Could not load db.properties: " + ex.getMessage());
                ex.printStackTrace();
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
