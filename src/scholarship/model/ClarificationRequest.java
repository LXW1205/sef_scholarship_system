package scholarship.model;

import java.sql.Timestamp;

public class ClarificationRequest {
    private int reqID;
    private int evalID;
    private String question;
    private String answer;
    private String status;
    private Timestamp requestedDate;
    private Timestamp answeredDate;

    public ClarificationRequest(int reqID, int evalID, String question, String answer, String status,
            Timestamp requestedDate, Timestamp answeredDate) {
        this.reqID = reqID;
        this.evalID = evalID;
        this.question = question;
        this.answer = answer;
        this.status = status;
        this.requestedDate = requestedDate;
        this.answeredDate = answeredDate;
    }

    public int getReqID() {
        return reqID;
    }

    public int getEvalID() {
        return evalID;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getRequestedDate() {
        return requestedDate;
    }

    public Timestamp getAnsweredDate() {
        return answeredDate;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAnsweredDate(Timestamp answeredDate) {
        this.answeredDate = answeredDate;
    }
}
