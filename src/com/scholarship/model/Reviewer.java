package com.scholarship.model;

public class Reviewer extends User {
    private String staffID;
    private String department;

    public Reviewer(int id, String username, String email, boolean isActive, String staffID, String department) {
        super(id, username, email, "Reviewer", isActive);
        this.staffID = staffID;
        this.department = department;
    }

    public String getStaffID() { return staffID; }
    public void setStaffID(String staffID) { this.staffID = staffID; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public boolean login() { return true; }
    @Override
    public void logout() { }
}
