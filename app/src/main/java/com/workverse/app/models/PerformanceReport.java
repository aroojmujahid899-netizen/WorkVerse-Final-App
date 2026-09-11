package com.workverse.app.models;

import java.util.HashMap;
import java.util.Map;

public class PerformanceReport {
    private String id, userId, employeeName, month, campaign;
    private Object year; // Object avoids type conversion crashes between String/Integer in Firestore
    private int tasksCompleted, tasksAssigned;
    private double kpiScore, attendancePercentage;

    private String feedbackSentiment;
    private Object salesPerformance;
    private String performanceInsight;
    private Object feedbackAccuracy;
    private Long timestamp;
    private Integer totalSales;
    private Integer totalCalls;

    private Double aiFeedbackScore;
    private Double salesScore;
    private Double finalKpiScore;
    private Map<String, Double> kpiWeightsUsed = new HashMap<>();
    private Long kpiComputedAt;

    public PerformanceReport() {}

    public String getId() { return id; }
    public void setId(String v) { id = v; }

    public String getUserId() { return userId; }
    public void setUserId(String v) { userId = v; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { employeeName = v; }

    public String getMonth() { return month; }
    public void setMonth(String v) { month = v; }

    public String getCampaign() { return campaign; }
    public void setCampaign(String v) { campaign = v; }

    public Object getYear() { return year; }
    public void setYear(Object v) { year = v; }

    public String getYearString() {
        return year != null ? String.valueOf(year) : "";
    }

    public int getTasksCompleted() { return tasksCompleted; }
    public void setTasksCompleted(int v) { tasksCompleted = v; }

    public int getTasksAssigned() { return tasksAssigned; }
    public void setTasksAssigned(int v) { tasksAssigned = v; }

    public double getKpiScore() { return kpiScore; }
    public void setKpiScore(double v) { kpiScore = v; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double v) { attendancePercentage = v; }

    public Double getAiFeedbackScore() { return aiFeedbackScore; }
    public void setAiFeedbackScore(Double v) { aiFeedbackScore = v; }

    public Double getSalesScore() { return salesScore; }
    public void setSalesScore(Double v) { salesScore = v; }

    public Double getFinalKpiScore() { return finalKpiScore; }
    public void setFinalKpiScore(Double v) { finalKpiScore = v; }

    public Map<String, Double> getKpiWeightsUsed() { return kpiWeightsUsed; }
    public void setKpiWeightsUsed(Map<String, Double> v) { kpiWeightsUsed = v; }

    public Long getKpiComputedAt() { return kpiComputedAt; }
    public void setKpiComputedAt(Long v) { kpiComputedAt = v; }

    public String getFeedbackSentiment() { return feedbackSentiment; }
    public void setFeedbackSentiment(String v) { feedbackSentiment = v; }

    public Double getSalesPerformance() {
        if (salesPerformance == null) return null;
        if (salesPerformance instanceof Number) return ((Number) salesPerformance).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(salesPerformance).replace("%", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
    public void setSalesPerformance(Object v) { salesPerformance = v; }

    public String getPerformanceInsight() { return performanceInsight; }
    public void setPerformanceInsight(String v) { performanceInsight = v; }

    public Double getFeedbackAccuracy() {
        if (feedbackAccuracy == null) return null;
        if (feedbackAccuracy instanceof Number) return ((Number) feedbackAccuracy).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(feedbackAccuracy).replace("%", "").trim());
        } catch (Exception e) {
            return null;
        }
    }
    public void setFeedbackAccuracy(Object v) { feedbackAccuracy = v; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long v) { timestamp = v; }

    public Integer getTotalSales() { return totalSales; }
    public void setTotalSales(Integer v) { totalSales = v; }

    public Integer getTotalCalls() { return totalCalls; }
    public void setTotalCalls(Integer v) { totalCalls = v; }

    public double getDisplayKpi() {
        return finalKpiScore != null ? finalKpiScore : kpiScore;
    }
}