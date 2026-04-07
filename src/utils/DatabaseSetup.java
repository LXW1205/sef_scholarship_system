package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;

public class DatabaseSetup {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            Properties props = new Properties();

            System.out.println("========================================");
            System.out.println("   SEF Scholarship System Database Setup");
            System.out.println("========================================");

            // Check for Environment Variables (for Render/Cloud)
            String envUrl = System.getenv("DB_URL");
            if (envUrl == null) envUrl = System.getenv("DATABASE_URL");
            
            String envUser = System.getenv("DB_USER");
            String envPassword = System.getenv("DB_PASSWORD");

            String url, user, password;

            if (envUrl != null) {
                System.out.println("[INFO] Environment variable detected. Using automatic setup...");
                
                try {
                    String uriString = envUrl;
                    if (uriString.startsWith("jdbc:postgresql://")) {
                        uriString = uriString.substring(5);
                    } else if (!uriString.contains("://")) {
                        uriString = "postgresql://" + uriString;
                    }
                    
                    URI uri = new URI(uriString);
                    String host = uri.getHost();
                    int port = uri.getPort();
                    if (port == -1) port = 5432;
                    String path = uri.getPath();
                    String userInfo = uri.getUserInfo();
                    
                    url = "jdbc:postgresql://" + host + ":" + port + path;
                    
                    if (envUser != null) {
                        user = envUser;
                    } else if (userInfo != null && userInfo.contains(":")) {
                        user = userInfo.split(":")[0];
                    } else {
                        user = (userInfo != null) ? userInfo : "";
                    }

                    if (envPassword != null) {
                        password = envPassword;
                    } else if (userInfo != null && userInfo.contains(":")) {
                        password = userInfo.split(":")[1];
                    } else {
                        password = "";
                    }

                } catch (URISyntaxException e) {
                    System.err.println("[ERROR] Failed to parse DB URL: " + envUrl);
                    url = envUrl;
                    user = (envUser != null) ? envUser : "";
                    password = (envPassword != null) ? envPassword : "";
                }

            } else {
                // 1. Ask for JDBC URL
                System.out.println("\nStep 1: Database URL");
                System.out.println("Hint: jdbc:postgresql://127.0.0.1:5432/your_db_name");
                System.out.print("Enter Database URL: ");
                url = scanner.nextLine().trim();

                // Ensure the URL starts with jdbc:
                if (!url.startsWith("jdbc:")) {
                    if (url.startsWith("postgresql:")) {
                        url = "jdbc:" + url;
                    } else {
                        url = "jdbc:postgresql://" + url;
                    }
                    System.out.println("-> Adjusted URL to: " + url);
                }

                // 2. Ask for Username
                System.out.println("\nStep 2: Database User");
                System.out.println("Hint: Default is usually 'postgres'");
                System.out.print("Enter Database User: ");
                user = scanner.nextLine().trim();

                // 3. Ask for Password
                System.out.println("\nStep 3: Database Password");
                System.out.print("Enter Database Password: ");
                password = scanner.nextLine().trim();

                // 4. Save to db.properties (only locally)
                try (FileOutputStream out = new FileOutputStream("db.properties")) {
                    props.setProperty("db.url", url);
                    props.setProperty("db.user", user);
                    props.setProperty("db.password", password);
                    props.store(out, "Database Configuration");
                    System.out.println("\n[SUCCESS] db.properties file created.");
                } catch (IOException e) {
                    System.err.println("\n[ERROR] Failed to save db.properties: " + e.getMessage());
                }
            }

            // 5. Test Connection
            System.out.println("\nStep 4: Testing Connection...");
            try {
                Class.forName("org.postgresql.Driver");
                try (Connection conn = DriverManager.getConnection(url, user, password)) {
                    System.out.println("[SUCCESS] Connection established.");

                    // 6. Populate Database
                    String choice = "y";
                    if (envUrl == null) {
                        System.out.print(
                                "\nDo you want to populate the database with project tables and sample data? (y/n): ");
                        choice = scanner.nextLine().trim().toLowerCase();
                    }

                    if (choice.equals("y")) {
                        System.out.println("\nStep 5: Populating Database...");

                        executeSqlScript(conn, "sql_queries/creation_tables.sql");
                        System.out.println("[SUCCESS] Tables created.");

                        executeSqlScript(conn, "sql_queries/sample_data.sql");
                        System.out.println("[SUCCESS] Sample data populated.");
                    } else {
                        System.out.println("\n[INFO] Skipping database population.");
                    }

                    System.out.println("\n========================================");
                    System.out.println("   Database Setup Completed Successfully!");
                    System.out.println("========================================");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("[ERROR] PostgreSQL Driver not found in classpath.");
            } catch (SQLException e) {
                System.err.println("[ERROR] Database connection failed: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("[ERROR] Failed to read SQL scripts: " + e.getMessage());
            }
        } finally {
            scanner.close();
        }
    }

    private static void executeSqlScript(Connection conn, String filePath) throws IOException, SQLException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        // Split by semicolon, but be careful with strings/comments if necessary
        // Simple split for basic SQL scripts
        String[] statements = content.split(";");
        try (Statement st = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    try {
                        st.execute(sql);
                    } catch (SQLException e) {
                        System.err.println("\n[SQL ERROR] Failed in file: " + filePath);
                        System.err.println("Statement: " + sql);
                        System.err.println("Reason: " + e.getMessage());
                        // Optional: don't throw if you want to continue despite errors
                        throw e;
                    }
                }
            }
        }
    }
}
