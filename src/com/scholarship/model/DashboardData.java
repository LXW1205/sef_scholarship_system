package com.scholarship.model;

public class DashboardData {
    private int totalUsers;
    private int activeScholarships;
    private int pendingApps;
    private int approvedMonth;

    public DashboardData(int totalUsers, int activeScholarships, int pendingApps, int approvedMonth) {
        this.totalUsers = totalUsers;
        this.activeScholarships = activeScholarships;
        this.pendingApps = pendingApps;
        this.approvedMonth = approvedMonth;
    }

    public int getTotalUsers() { return totalUsers; }
    public int getActiveScholarships() { return activeScholarships; }
    public int getPendingApps() { return pendingApps; }
    public int getApprovedMonth() { return approvedMonth; }
}
