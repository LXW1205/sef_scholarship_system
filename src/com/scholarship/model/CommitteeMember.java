package com.scholarship.model;

public class CommitteeMember extends User {
    private int memberID;
    private String position;

    public CommitteeMember(int id, String username, String email, boolean isActive, int memberID, String position) {
        super(id, username, email, "CommitteeMember", isActive);
        this.memberID = memberID;
        this.position = position;
    }

    public int getMemberID() { return memberID; }
    public void setMemberID(int memberID) { this.memberID = memberID; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    @Override
    public boolean login() { return true; }
    @Override
    public void logout() { }
}
