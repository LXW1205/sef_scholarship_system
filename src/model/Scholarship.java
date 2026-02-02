package model;

import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

public class Scholarship {
    private int scholarshipID;
    private String title;
    private String description;
    private String amount;
    private String forQualification;
    private Date deadline;
    private double minCGPA;
    private double maxFamilyIncome;
    private boolean isActive;
    private List<Criterion> criteria;

    public Scholarship() {
        this.criteria = new ArrayList<>();
    }

    public Scholarship(int scholarshipID, String title, String description, String amount, String forQualification,
            Date deadline, double minCGPA, double maxFamilyIncome, boolean isActive) {
        this.scholarshipID = scholarshipID;
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.forQualification = forQualification;
        this.deadline = deadline;
        this.minCGPA = minCGPA;
        this.maxFamilyIncome = maxFamilyIncome;
        this.isActive = isActive;
        this.criteria = new ArrayList<>();
    }

    public int getScholarshipID() {
        return scholarshipID;
    }

    public void setScholarshipID(int scholarshipID) {
        this.scholarshipID = scholarshipID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getForQualification() {
        return forQualification;
    }

    public void setForQualification(String forQualification) {
        this.forQualification = forQualification;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public double getMinCGPA() {
        return minCGPA;
    }

    public void setMinCGPA(double minCGPA) {
        this.minCGPA = minCGPA;
    }

    public double getMaxFamilyIncome() {
        return maxFamilyIncome;
    }

    public void setMaxFamilyIncome(double maxFamilyIncome) {
        this.maxFamilyIncome = maxFamilyIncome;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
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
