package model;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Student extends User {
    private String studentID;
    private double cgpa;
    private String major;
    private String qualification;
    private String yearOfStudy;
    private String expectedGraduation;
    private double familyIncome;

    public Student(int id, String fullName, String email, boolean isActive, String studentID, double cgpa) {
        this(id, fullName, email, isActive, studentID, cgpa, null, null, null, null, 0.0);
    }

    public Student(int id, String fullName, String email, boolean isActive, String studentID, double cgpa,
            String major, String qualification, String yearOfStudy, String expectedGraduation, double familyIncome) {
        super(id, fullName, email, "Student", isActive);
        this.studentID = studentID;
        this.cgpa = cgpa;
        this.major = major;
        this.qualification = qualification;
        this.yearOfStudy = yearOfStudy;
        this.expectedGraduation = expectedGraduation;
        this.familyIncome = familyIncome;
    }

    // Default constructor for testing/mocking
    public Student() {
        super();
        this.role = "Student";
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public double getCgpa() {
        return cgpa;
    }

    public void setCgpa(double cgpa) {
        this.cgpa = cgpa;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getExpectedGraduation() {
        return expectedGraduation;
    }

    public void setExpectedGraduation(String expectedGraduation) {
        this.expectedGraduation = expectedGraduation;
    }

    public double getFamilyIncome() {
        return familyIncome;
    }

    public void setFamilyIncome(double familyIncome) {
        this.familyIncome = familyIncome;
    }

    public List<Scholarship> viewAvailableScholarships() {
        List<Scholarship> scholarships = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Scholarship WHERE isActive = true ORDER BY deadline")) {

            while (rs.next()) {
                Scholarship scholarship = new Scholarship(
                        rs.getInt("scholarshipID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("amount"), // Changed to String
                        rs.getString("forQualification"), // Added field
                        rs.getDate("deadline"),
                        rs.getDouble("minCGPA"),
                        rs.getDouble("maxFamilyIncome"),
                        rs.getBoolean("requiresInterview"),
                        rs.getBoolean("isActive"));

                // Fetch criteria for this scholarship
                int scholarshipID = rs.getInt("scholarshipID");
                try (Statement criteriaStmt = conn.createStatement();
                        ResultSet criteriaRs = criteriaStmt.executeQuery(
                                "SELECT * FROM Criteria WHERE scholarshipID = " + scholarshipID)) {

                    while (criteriaRs.next()) {
                        scholarship.addCriterion(new Criterion(
                                criteriaRs.getInt("criteriaID"),
                                criteriaRs.getInt("scholarshipID"),
                                criteriaRs.getString("name"),
                                criteriaRs.getInt("weightage"),
                                criteriaRs.getDouble("maxScore")));
                    }
                }

                scholarships.add(scholarship);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scholarships;
    }

    public Scholarship getScholarshipById(int id) {
        Scholarship scholarship = null;
        String sql = "SELECT * FROM Scholarship WHERE scholarshipID = " + id;
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                scholarship = new Scholarship(
                        rs.getInt("scholarshipID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("amount"), // Changed to String
                        rs.getString("forQualification"), // Added field
                        rs.getDate("deadline"),
                        rs.getDouble("minCGPA"),
                        rs.getDouble("maxFamilyIncome"),
                        rs.getBoolean("requiresInterview"),
                        rs.getBoolean("isActive"));

                // Fetch criteria
                try (Statement criteriaStmt = conn.createStatement();
                        ResultSet criteriaRs = criteriaStmt.executeQuery(
                                "SELECT * FROM Criteria WHERE scholarshipID = " + id)) {

                    while (criteriaRs.next()) {
                        scholarship.addCriterion(new Criterion(
                                criteriaRs.getInt("criteriaID"),
                                criteriaRs.getInt("scholarshipID"),
                                criteriaRs.getString("name"),
                                criteriaRs.getInt("weightage"),
                                criteriaRs.getDouble("maxScore")));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scholarship;
    }

    public List<Application> viewAppHistory() {
        List<Application> applications = new ArrayList<>();
        String sql = "SELECT a.appID, a.scholarshipID, s.title as scholarship_title, a.submissionDate, a.status " +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "WHERE a.studentID = '" + studentID + "' " +
                "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                applications.add(new Application(
                        rs.getInt("appID"),
                        this.studentID,
                        rs.getInt("scholarshipID"),
                        rs.getString("scholarship_title"),
                        rs.getTimestamp("submissionDate"),
                        rs.getString("status")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return applications;
    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public void logout() {
    }
}
