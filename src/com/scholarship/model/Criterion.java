package com.scholarship.model;

public class Criterion {
    private int criteriaID;
    private int scholarshipID;
    private String name;
    private int weightage;
    private double maxScore;

    public Criterion() {}

    public Criterion(int criteriaID, int scholarshipID, String name, int weightage, double maxScore) {
        this.criteriaID = criteriaID;
        this.scholarshipID = scholarshipID;
        this.name = name;
        this.weightage = weightage;
        this.maxScore = maxScore;
    }

    public int getCriteriaID() { return criteriaID; }
    public void setCriteriaID(int criteriaID) { this.criteriaID = criteriaID; }

    public int getScholarshipID() { return scholarshipID; }
    public void setScholarshipID(int scholarshipID) { this.scholarshipID = scholarshipID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getWeightage() { return weightage; }
    public void setWeightage(int weightage) { this.weightage = weightage; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
}
