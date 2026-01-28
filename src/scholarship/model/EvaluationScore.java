package scholarship.model;

public class EvaluationScore {
    private int scoreID;
    private int evalID;
    private int criteriaID;
    private double score;
    private String criteriaName; // Helper for display

    public EvaluationScore() {
    }

    public EvaluationScore(int scoreID, int evalID, int criteriaID, double score) {
        this.scoreID = scoreID;
        this.evalID = evalID;
        this.criteriaID = criteriaID;
        this.score = score;
    }

    public int getScoreID() {
        return scoreID;
    }

    public void setScoreID(int scoreID) {
        this.scoreID = scoreID;
    }

    public int getEvalID() {
        return evalID;
    }

    public void setEvalID(int evalID) {
        this.evalID = evalID;
    }

    public int getCriteriaID() {
        return criteriaID;
    }

    public void setCriteriaID(int criteriaID) {
        this.criteriaID = criteriaID;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getCriteriaName() {
        return criteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        this.criteriaName = criteriaName;
    }
}
