package com.scholarship.model;

import java.sql.Date;

public class Scholarship {
    private int scholarshipID;
    private String title;
    private Date deadline;
    private boolean isActive;

    public Scholarship(int scholarshipID, String title, Date deadline, boolean isActive) {
        this.scholarshipID = scholarshipID;
        this.title = title;
        this.deadline = deadline;
        this.isActive = isActive;
    }

    public int getScholarshipID() { return scholarshipID; }
    public String getTitle() { return title; }
    public Date getDeadline() { return deadline; }
    public boolean isActive() { return isActive; }
}
