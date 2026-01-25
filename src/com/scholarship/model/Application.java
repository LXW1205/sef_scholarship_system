package com.scholarship.model;

import java.sql.Timestamp;

public class Application {
    private int appID;
    private String studentID; // In UML linked to Student, here just ID reference for simplicity unless we
                              // fetch object
    private int scholarshipID; // ID reference
    private String scholarshipTitle; // Extra field for display convenience (from join)
    private Timestamp submissionDate;
    private String status;
    private String reviewerID; // Staff ID of assigned reviewer
    private String reviewerName; // Full name of assigned reviewer
    private String applicantName; // Full name of the applicant
    private String applicantEmail;
    private double cgpa;
    private String major;
    private String yearOfStudy;
    private String qualification;
    private double familyIncome;
    private java.util.List<Document> documents = new java.util.ArrayList<>();

    private String personalStatement;
    private String otherScholarships;
    private String decisionComments;

    public Application(int appID, String studentID, int scholarshipID, String scholarshipTitle,
            Timestamp submissionDate, String status) {
        this(appID, studentID, scholarshipID, scholarshipTitle, submissionDate, status, null, null, null);
    }

    public Application(int appID, String studentID, int scholarshipID, String scholarshipTitle,
            Timestamp submissionDate, String status, String reviewerID, String reviewerName, String applicantName) {
        this.appID = appID;
        this.studentID = studentID;
        this.scholarshipID = scholarshipID;
        this.scholarshipTitle = scholarshipTitle;
        this.submissionDate = submissionDate;
        this.status = status;
        this.reviewerID = reviewerID;
        this.reviewerName = reviewerName;
        this.applicantName = applicantName;
    }

    public int getAppID() {
        return appID;
    }

    public String getStudentID() {
        return studentID;
    }

    public int getScholarshipID() {
        return scholarshipID;
    }

    public String getScholarshipTitle() {
        return scholarshipTitle;
    }

    public Timestamp getSubmissionDate() {
        return submissionDate;
    }

    public String getStatus() {
        return status;
    }

    public String getReviewerID() {
        return reviewerID;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String email) {
        this.applicantEmail = email;
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

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getFamilyIncome() {
        return familyIncome;
    }

    public void setFamilyIncome(double familyIncome) {
        this.familyIncome = familyIncome;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public java.util.List<Document> getDocuments() {
        return documents;
    }

    public void addDocument(Document d) {
        this.documents.add(d);
    }

    public void setDocuments(java.util.List<Document> documents) {
        this.documents = documents;
    }

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }

    public String getOtherScholarships() {
        return otherScholarships;
    }

    public void setOtherScholarships(String otherScholarships) {
        this.otherScholarships = otherScholarships;
    }

    public String getDecisionComments() {
        return decisionComments;
    }

    public void setDecisionComments(String decisionComments) {
        this.decisionComments = decisionComments;
    }
}
