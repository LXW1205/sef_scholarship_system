package scholarship.model;

import java.sql.Timestamp;

public class AuditLog {
    private int logID;
    private Integer userID;
    private String userEmail;
    private String action;
    private String entityType;
    private String entityID;
    private String details;
    private String ipAddress;
    private Timestamp createdAt;

    public AuditLog() {
    }

    public AuditLog(Integer userID, String userEmail, String action, String entityType,
            String entityID, String details, String ipAddress) {
        this.userID = userID;
        this.userEmail = userEmail;
        this.action = action;
        this.entityType = entityType;
        this.entityID = entityID;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    // Getters and Setters
    public int getLogID() {
        return logID;
    }

    public void setLogID(int logID) {
        this.logID = logID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
