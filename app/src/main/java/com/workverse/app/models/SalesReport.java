package com.workverse.app.models;

public class SalesReport {
    private String id, userId, employeeName, date, month;
    private double targetAmount, achievedAmount;
    private long timestamp;

    public SalesReport() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }

    public String getUserId() { return userId; }
    public void setUserId(String v) { userId = v; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { employeeName = v; }

    public String getDate() { return date; }
    public void setDate(String v) { date = v; }

    public String getMonth() { return month; }
    public void setMonth(String v) { month = v; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double v) { targetAmount = v; }

    public double getAchievedAmount() { return achievedAmount; }
    public void setAchievedAmount(double v) { achievedAmount = v; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long v) { timestamp = v; }

    // getAmount() = alias for achievedAmount
    public double getAmount() {
        return achievedAmount;
    }
}