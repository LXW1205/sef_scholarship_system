package model;

import java.sql.Timestamp;

public class Evaluation {
    private int evalID;
    private int appID;
    private String reviewerID;
    private String scholarshipComments;
    private float interviewScore;
    private String interviewComments;
    private String status;
    private Timestamp evaluatedDate;

    public Evaluation(int evalID, int appID, String reviewerID, String scholarshipComments,
            float interviewScore, String interviewComments, String status, Timestamp evaluatedDate) {
        this.evalID = evalID;
        this.appID = appID;
        this.reviewerID = reviewerID;
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

    public String getReviewerID() {
        return reviewerID;
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
