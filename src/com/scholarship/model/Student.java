package com.scholarship.model;

import com.scholarship.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private String studentID;
    private String fullName;
    private double cgpa;

    public Student(int id, String username, String email, boolean isActive, String studentID, String fullName, double cgpa) {
        super(id, username, email, "Student", isActive);
        this.studentID = studentID;
        this.fullName = fullName;
        this.cgpa = cgpa;
    }
    
    // Default constructor for testing/mocking
    public Student() { 
        super();
        this.role = "Student";
    }

    public String getStudentID() { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public List<Scholarship> viewAvailableScholarships() {
        List<Scholarship> scholarships = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Scholarship WHERE isActive = true ORDER BY deadline")) {
            
            while (rs.next()) {
                scholarships.add(new Scholarship(
                    rs.getInt("scholarshipID"),
                    rs.getString("title"),
                    rs.getDate("deadline"),
                    rs.getBoolean("isActive")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scholarships;
    }

    public List<Application> viewAppHistory() {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.appID, a.scholarshipID, s.title as scholarship_title, a.submissionDate, a.status " +
                     "FROM Application a " +
                     "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                     // "WHERE a.studentID = '" + this.studentID + "' " + // Uncomment in real app to filter by this user
                     "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                applications.add(new Application(
                    rs.getInt("appID"),
                    this.studentID, // Ideally from DB, but for now we assume it's this student's apps (or all as per original code)
                    rs.getInt("scholarshipID"),
                    rs.getString("scholarship_title"),
                    rs.getTimestamp("submissionDate"),
                    rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applications;
    }

    @Override
    public boolean login() { return true; }
    @Override
    public void logout() { }
}
