package com.scholarship.model;

import java.sql.Timestamp;

public class Inquiry {
    private int inquiryID;
    private String studentID;
    private String message;
    private Timestamp submittedAt;

    public Inquiry(int inquiryID, String studentID, String message, Timestamp submittedAt) {
        this.inquiryID = inquiryID;
        this.studentID = studentID;
        this.message = message;
        this.submittedAt = submittedAt;
    }

    public int getInquiryID() {
        return inquiryID;
    }

    public String getStudentID() {
        return studentID;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }
}
