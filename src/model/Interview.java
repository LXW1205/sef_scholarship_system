package model;

import java.sql.Timestamp;

public class Interview {
    private int interviewID;
    private int evalID;
    private Timestamp dateTime;
    private String venueOrLink;
    private String status;

    public Interview(int interviewID, int evalID, Timestamp dateTime, String venueOrLink, String status) {
        this.interviewID = interviewID;
        this.evalID = evalID;
        this.dateTime = dateTime;
        this.venueOrLink = venueOrLink;
        this.status = status;
    }

    public int getInterviewID() {
        return interviewID;
    }

    public int getEvalID() {
        return evalID;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public String getVenueOrLink() {
        return venueOrLink;
    }

    public String getStatus() {
        return status;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public void setVenueOrLink(String venueOrLink) {
        this.venueOrLink = venueOrLink;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
