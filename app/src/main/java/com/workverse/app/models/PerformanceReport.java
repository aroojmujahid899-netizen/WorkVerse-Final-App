package com.workverse.app.models;

public class PerformanceReport {
    private String id, userId, employeeName, month, year, campaign;
    private int    tasksCompleted, tasksAssigned;
    private double kpiScore, attendancePercentage;
    private String feedbackSentiment;
    private String feedbackAccuracy;
    private String performanceInsight;
    private double salesPerformance;
    private long   timestamp;

    public PerformanceReport() {}

    public String getId()                    { return id; }                   public void setId(String v)                    { id = v; }
    public String getUserId()                { return userId; }               public void setUserId(String v)                { userId = v; }
    public String getEmployeeName()          { return employeeName; }         public void setEmployeeName(String v)          { employeeName = v; }
    public String getMonth()                 { return month; }                public void setMonth(String v)                 { month = v; }
    public String getYear()                  { return year; }                 public void setYear(String v)                  { year = v; }
    public String getCampaign()              { return campaign; }             public void setCampaign(String v)              { campaign = v; }
    public int    getTasksCompleted()        { return tasksCompleted; }       public void setTasksCompleted(int v)           { tasksCompleted = v; }
    public int    getTasksAssigned()         { return tasksAssigned; }        public void setTasksAssigned(int v)            { tasksAssigned = v; }
    public double getKpiScore()              { return kpiScore; }             public void setKpiScore(double v)              { kpiScore = v; }
    public double getAttendancePercentage()  { return attendancePercentage; } public void setAttendancePercentage(double v)  { attendancePercentage = v; }
    public String getFeedbackSentiment()     { return feedbackSentiment; }    public void setFeedbackSentiment(String v)     { feedbackSentiment = v; }
    public String getFeedbackAccuracy()      { return feedbackAccuracy; }     public void setFeedbackAccuracy(String v)      { feedbackAccuracy = v; }
    public String getPerformanceInsight()    { return performanceInsight; }   public void setPerformanceInsight(String v)    { performanceInsight = v; }
    public double getSalesPerformance()      { return salesPerformance; }     public void setSalesPerformance(double v)      { salesPerformance = v; }
    public long   getTimestamp()             { return timestamp; }            public void setTimestamp(long v)               { timestamp = v; }
}