package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class DatabaseConnection {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        String envUrl = System.getenv("DB_URL");
        if (envUrl == null) envUrl = System.getenv("DATABASE_URL");
        
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        if (envUrl != null) {
            try {
                // Handle postgres:// or postgresql:// or jdbc:postgresql://
                String uriString = envUrl;
                if (uriString.startsWith("jdbc:postgresql://")) {
                    uriString = uriString.substring(5); // Remove jdbc: to parse with URI
                } else if (!uriString.contains("://")) {
                    uriString = "postgresql://" + uriString;
                }
                
                URI uri = new URI(uriString);
                String host = uri.getHost();
                int port = uri.getPort();
                if (port == -1) port = 5432;
                String path = uri.getPath(); // /database
                String userInfo = uri.getUserInfo(); // user:password
                
                // Reconstruct standard JDBC URL
                URL = "jdbc:postgresql://" + host + ":" + port + path;
                
                // Prioritize explicit variables, fallback to URI userInfo
                if (envUser != null) {
                    USER = envUser;
                } else if (userInfo != null && userInfo.contains(":")) {
                    USER = userInfo.split(":")[0];
                } else {
                    USER = (userInfo != null) ? userInfo : "";
                }

                if (envPassword != null) {
                    PASSWORD = envPassword;
                } else if (userInfo != null && userInfo.contains(":")) {
                    PASSWORD = userInfo.split(":")[1];
                } else {
                    PASSWORD = "";
                }
                
                System.out.println("[DB] Parsed cloud URL successfully. Host: " + host);

            } catch (URISyntaxException e) {
                System.err.println("[DB] Failed to parse cloud URL: " + envUrl);
                URL = envUrl; // Fallback
                USER = (envUser != null) ? envUser : "";
                PASSWORD = (envPassword != null) ? envPassword : "";
            }
        } else {
            Properties prop = new Properties();
            try (FileInputStream input = new FileInputStream("db.properties")) {
                prop.load(input);
                URL = prop.getProperty("db.url");
                USER = prop.getProperty("db.user");
                PASSWORD = prop.getProperty("db.password");
                System.out.println("[DB] Loaded from properties: " + URL);
            } catch (IOException ex) {
                System.err.println("[DB] Error: No credentials found (check env vars or db.properties).");
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
