package com.scholarship.model;

public class Reviewer extends User {
    private String reviewerID;
    private String department;

    public Reviewer(int id, String fullName, String email, boolean isActive, String reviewerID, String department) {
        super(id, fullName, email, "Reviewer", isActive);
        this.reviewerID = reviewerID;
        this.department = department;
    }

    public Reviewer() {
        super();
        this.role = "Reviewer";
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public void setReviewerID(String reviewerID) {
        this.reviewerID = reviewerID;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public void logout() {
    }
}
