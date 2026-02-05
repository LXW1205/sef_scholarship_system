package model;

import java.sql.Timestamp;

public class Inquiry {
    private int inquiryID;
    private String studentID;
    private String message;
    private Timestamp submittedAt;
    private String status;
    private String answer;
    private Timestamp answeredAt;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Timestamp getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Timestamp answeredAt) {
        this.answeredAt = answeredAt;
    }

}
