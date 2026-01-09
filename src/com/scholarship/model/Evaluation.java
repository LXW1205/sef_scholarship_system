package com.scholarship.model;

import java.sql.Timestamp;

public class Evaluation {
    private int evalID;
    private int appID;
    private String reviewerStaffID;
    private float scholarshipScore;
    private String scholarshipComments;
    private float interviewScore;
    private String interviewComments;
    private String status;
    private Timestamp evaluatedDate;

    public Evaluation(int evalID, int appID, String reviewerStaffID, float scholarshipScore, String scholarshipComments,
            float interviewScore, String interviewComments, String status, Timestamp evaluatedDate) {
        this.evalID = evalID;
        this.appID = appID;
        this.reviewerStaffID = reviewerStaffID;
        this.scholarshipScore = scholarshipScore;
        this.scholarshipComments = scholarshipComments;
        this.interviewScore = interviewScore;
        this.interviewComments = interviewComments;
        this.status = status;
        this.evaluatedDate = evaluatedDate;
    }

    public int getEvalID() {
        return evalID;
    }

    public int getAppID() {
        return appID;
    }

    public String getReviewerStaffID() {
        return reviewerStaffID;
    }

    public float getScholarshipScore() {
        return scholarshipScore;
    }

    public String getScholarshipComments() {
        return scholarshipComments;
    }

    public float getInterviewScore() {
        return interviewScore;
    }

    public String getInterviewComments() {
        return interviewComments;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getEvaluatedDate() {
        return evaluatedDate;
    }

    public void setScholarshipScore(float scholarshipScore) {
        this.scholarshipScore = scholarshipScore;
    }

    public void setScholarshipComments(String scholarshipComments) {
        this.scholarshipComments = scholarshipComments;
    }

    public void setInterviewScore(float interviewScore) {
        this.interviewScore = interviewScore;
    }

    public void setInterviewComments(String interviewComments) {
        this.interviewComments = interviewComments;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
