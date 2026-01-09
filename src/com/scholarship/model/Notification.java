package com.scholarship.model;

import java.sql.Timestamp;

public class Notification {
    private int notifID;
    private int userID;
    private String message;
    private Timestamp sentAt;
    private boolean isRead;

    public Notification(int notifID, int userID, String message, Timestamp sentAt, boolean isRead) {
        this.notifID = notifID;
        this.userID = userID;
        this.message = message;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    public int getNotifID() {
        return notifID;
    }

    public int getUserID() {
        return userID;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
