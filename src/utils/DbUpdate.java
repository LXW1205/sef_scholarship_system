package utils;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DbUpdate {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Adding columns to Scholarship table...");
            
            try {
                stmt.executeUpdate("ALTER TABLE Scholarship ADD COLUMN description TEXT");
                System.out.println("Added description column.");
            } catch (Exception e) {
                System.out.println("Description column might already exist: " + e.getMessage());
            }
            
            try {
                stmt.executeUpdate("ALTER TABLE Scholarship ADD COLUMN amount DECIMAL(10,2)");
                System.out.println("Added amount column.");
            } catch (Exception e) {
                System.out.println("Amount column might already exist: " + e.getMessage());
            }
            
            System.out.println("Database update complete.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
