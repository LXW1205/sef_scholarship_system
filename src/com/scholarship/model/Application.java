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
    private java.util.List<Document> documents = new java.util.ArrayList<>();

    public Application(int appID, String studentID, int scholarshipID, String scholarshipTitle,
            Timestamp submissionDate, String status) {
        this.appID = appID;
        this.studentID = studentID;
        this.scholarshipID = scholarshipID;
        this.scholarshipTitle = scholarshipTitle;
        this.submissionDate = submissionDate;
        this.status = status;
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

    public java.util.List<Document> getDocuments() {
        return documents;
    }

    public void addDocument(Document d) {
        this.documents.add(d);
    }
}
