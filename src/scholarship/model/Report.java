package scholarship.model;

import java.sql.Timestamp;

public class Report {
    private int reportID;
    private String adminID;
    private String type;
    private String generatedFile;
    private Timestamp generatedDate;

    public Report(int reportID, String adminID, String type, String generatedFile, Timestamp generatedDate) {
        this.reportID = reportID;
        this.adminID = adminID;
        this.type = type;
        this.generatedFile = generatedFile;
        this.generatedDate = generatedDate;
    }

    public int getReportID() {
        return reportID;
    }

    public String getAdminID() {
        return adminID;
    }

    public String getType() {
        return type;
    }

    public String getGeneratedFile() {
        return generatedFile;
    }

    public Timestamp getGeneratedDate() {
        return generatedDate;
    }
}
