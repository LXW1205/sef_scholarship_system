package com.scholarship.model;

import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class Scholarship {
    private int scholarshipID;
    private String title;
    private String description;
    private double amount;
    private Date deadline;
    private boolean isActive;
    private List<Criterion> criteria;

    public Scholarship(int scholarshipID, String title, String description, double amount, Date deadline,
            boolean isActive) {
        this.scholarshipID = scholarshipID;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.deadline = deadline;
        this.isActive = isActive;
        this.criteria = new ArrayList<>();
    }

    public int getScholarshipID() {
        return scholarshipID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<Criterion> criteria) {
        this.criteria = criteria;
    }

    public void addCriterion(Criterion criterion) {
        this.criteria.add(criterion);
    }
}
