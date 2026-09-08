package com.workverse.app.models;

public class Attendance {
    private String id, userId, employeeName, date, checkInTime, checkOutTime, status, role, designation, campaign;

    public Attendance() {}

    public Attendance(String userId, String employeeName, String date, String checkInTime, String status) {
        this.userId = userId;
        this.employeeName = employeeName;
        this.date = date;
        this.checkInTime = checkInTime;
        this.status = status;
    }

    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { userId = v; }
    public String getEmployeeName() { return employeeName; } public void setEmployeeName(String v) { employeeName = v; }
    public String getDate() { return date; } public void setDate(String v) { date = v; }
    public String getCheckInTime() { return checkInTime; } public void setCheckInTime(String v) { checkInTime = v; }
    public String getCheckOutTime() { return checkOutTime; } public void setCheckOutTime(String v) { checkOutTime = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getRole() { return role; } public void setRole(String v) { role = v; }
    public String getDesignation() { return designation; } public void setDesignation(String v) { designation = v; }
    public String getCampaign() { return campaign; } public void setCampaign(String v) { campaign = v; }
}