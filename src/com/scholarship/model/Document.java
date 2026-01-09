package com.scholarship.model;

import java.sql.Timestamp;

public class Document {
    private int docID;
    private int appID;
    private String fileName;
    private String fileType;
    private Timestamp uploadDate;

    public Document(int docID, int appID, String fileName, String fileType, Timestamp uploadDate) {
        this.docID = docID;
        this.appID = appID;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadDate = uploadDate;
    }

    public int getDocID() {
        return docID;
    }

    public int getAppID() {
        return appID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }
}
