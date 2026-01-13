package com.scholarship.model;

public class DashboardData {
    private int totalUsers;
    private int activeScholarships;
    private int pendingApps;
    private int approvedMonth;
    private java.util.Map<String, Integer> trendData; // Label -> Count
    private java.util.Map<String, Integer> userRoles; // Role -> Count

    public DashboardData(int totalUsers, int activeScholarships, int pendingApps, int approvedMonth) {
        this(totalUsers, activeScholarships, pendingApps, approvedMonth, new java.util.HashMap<>(),
                new java.util.HashMap<>());
    }

    public DashboardData(int totalUsers, int activeScholarships, int pendingApps, int approvedMonth,
            java.util.Map<String, Integer> trendData, java.util.Map<String, Integer> userRoles) {
        this.totalUsers = totalUsers;
        this.activeScholarships = activeScholarships;
        this.pendingApps = pendingApps;
        this.approvedMonth = approvedMonth;
        this.trendData = trendData;
        this.userRoles = userRoles;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getActiveScholarships() {
        return activeScholarships;
    }

    public int getPendingApps() {
        return pendingApps;
    }

    public int getApprovedMonth() {
        return approvedMonth;
    }

    public java.util.Map<String, Integer> getTrendData() {
        return trendData;
    }

    public java.util.Map<String, Integer> getUserRoles() {
        return userRoles;
    }
}
