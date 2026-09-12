package com.workverse.app.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SalesReport {
    private String id;
    private String userId;
    private String employeeName;
    private String designation;
    private String campaign;
    private int targetAmount;
    private int achievedAmount;
    private long timestamp;

    public SalesReport() {
        // Required for Firebase
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCampaign() { return campaign; }
    public void setCampaign(String campaign) { this.campaign = campaign; }

    public int getTargetAmount() { return targetAmount; }
    public void setTargetAmount(int targetAmount) { this.targetAmount = targetAmount; }

    public int getAchievedAmount() { return achievedAmount; }
    public void setAchievedAmount(int achievedAmount) { this.achievedAmount = achievedAmount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDate() {
        if (timestamp == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}