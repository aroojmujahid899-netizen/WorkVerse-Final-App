package com.workverse.app.models;

public class Feedback {
    private String id, userId, fromUserName, title, message, role;
    private String sentiment;       // AI: Positive / Negative / Neutral
    private String accuracy;        // AI: High / Medium / Low
    private String insight;         // AI: performance insight text
    private long timestamp;

    public Feedback() {}

    public Feedback(String userId, String fromUserName, String title, String message, String role) {
        this.userId       = userId;
        this.fromUserName = fromUserName;
        this.title        = title;
        this.message      = message;
        this.role         = role;
        this.timestamp    = System.currentTimeMillis();
        // Run AI analysis on construction
        analyzeSentiment();
    }

    /** Run AI Feedback Analyzer to set sentiment, accuracy, insight */
    public void analyzeSentiment() {
        this.sentiment = com.workverse.app.utils.AIFeedbackAnalyzer.analyzeSentiment(this.message);
        // Default accuracy & insight — will be updated with actual calls/sales data if available
        this.accuracy  = "Medium";
        this.insight   = com.workverse.app.utils.AIFeedbackAnalyzer.generateInsight(this.sentiment, this.accuracy, 60);
    }

    /** Update accuracy and insight after cross-checking with actual performance data */
    public void updateWithPerformanceData(int totalCalls, int totalSales, double kpiScore) {
        this.accuracy = com.workverse.app.utils.AIFeedbackAnalyzer.calculateFeedbackAccuracy(this.sentiment, totalCalls, totalSales);
        this.insight  = com.workverse.app.utils.AIFeedbackAnalyzer.generateInsight(this.sentiment, this.accuracy, kpiScore);
    }

    // Getters & Setters
    public String getId()           { return id; }           public void setId(String v)           { id = v; }
    public String getUserId()       { return userId; }       public void setUserId(String v)       { userId = v; }
    public String getFromUserName() { return fromUserName; } public void setFromUserName(String v) { fromUserName = v; }
    public String getTitle()        { return title; }        public void setTitle(String v)        { title = v; }
    public String getMessage()      { return message; }      public void setMessage(String v)      { message = v; }
    public String getRole()         { return role; }         public void setRole(String v)         { role = v; }
    public String getSentiment()    { return sentiment; }    public void setSentiment(String v)    { sentiment = v; }
    public String getAccuracy()     { return accuracy; }     public void setAccuracy(String v)     { accuracy = v; }
    public String getInsight()      { return insight; }      public void setInsight(String v)      { insight = v; }
    public long getTimestamp()      { return timestamp; }    public void setTimestamp(long v)      { timestamp = v; }
}
