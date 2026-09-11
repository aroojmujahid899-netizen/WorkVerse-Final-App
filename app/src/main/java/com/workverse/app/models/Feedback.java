package com.workverse.app.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Feedback submitted by an Employee/Manager, plus the result of the REAL
 * AI analysis performed directly from the app (GeminiFeedbackService -> Google Gemini API).
 */
public class Feedback {
    private String id, userId, fromUserName, title, message, role;
    private String sentiment;
    private long timestamp;

    /** "pending" | "processing" | "completed" | "failed" */
    private String aiStatus = "pending";
    private String aiSentiment;              // "Positive" | "Neutral" | "Negative"
    private Object aiScore;                  // Safe Object handling
    private String aiSummary;
    private Object aiPositivePoints = new ArrayList<String>();
    private Object aiNegativePoints = new ArrayList<String>();
    private Object aiConcerns = new ArrayList<String>();
    private Object aiConfidence;             // Safe Object handling
    private String aiProvider;
    private String aiModel;
    private Long aiAnalyzedAt;
    private String aiError;

    private String insight;
    private Object accuracy;

    public Feedback() {}

    public Feedback(String userId, String fromUserName, String title, String message, String role) {
        this.userId = userId;
        this.fromUserName = fromUserName;
        this.title = title;
        this.message = message;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
        this.sentiment = "Neutral";
        this.aiStatus = "pending";
    }

    // Standard Getters and Setters
    public String getId(){ return id; } public void setId(String v){ id = v; }
    public String getUserId(){ return userId; } public void setUserId(String v){ userId = v; }
    public String getFromUserName(){ return fromUserName; } public void setFromUserName(String v){ fromUserName = v; }
    public String getTitle(){ return title; } public void setTitle(String v){ title = v; }
    public String getMessage(){ return message; } public void setMessage(String v){ message = v; }
    public String getRole(){ return role; } public void setRole(String v){ role = v; }
    public String getSentiment(){ return sentiment; } public void setSentiment(String v){ sentiment = v; }
    public long getTimestamp(){ return timestamp; } public void setTimestamp(long v){ timestamp = v; }

    public String getAiStatus(){ return aiStatus; } public void setAiStatus(String v){ aiStatus = v; }
    public String getAiSentiment(){ return aiSentiment; } public void setAiSentiment(String v){ aiSentiment = v; }

    // Safe getters & setters for numeric/object values
    public Object getAiScore(){ return aiScore; }
    public void setAiScore(Object v){
        if (v instanceof Number) {
            this.aiScore = ((Number) v).intValue();
        } else if (v instanceof String) {
            try {
                this.aiScore = Integer.parseInt((String) v);
            } catch (Exception e) {
                this.aiScore = 0;
            }
        } else {
            this.aiScore = v;
        }
    }

    public String getAiSummary(){ return aiSummary; } public void setAiSummary(String v){ aiSummary = v; }

    public Object getAiPositivePoints(){ return aiPositivePoints; }
    public void setAiPositivePoints(Object v){ this.aiPositivePoints = v; }

    public Object getAiNegativePoints(){ return aiNegativePoints; }
    public void setAiNegativePoints(Object v){ this.aiNegativePoints = v; }

    public Object getAiConcerns(){ return aiConcerns; }
    public void setAiConcerns(Object v){ this.aiConcerns = v; }

    public Object getAiConfidence(){ return aiConfidence; }
    public void setAiConfidence(Object v){
        if (v instanceof Number) {
            this.aiConfidence = ((Number) v).doubleValue();
        } else if (v instanceof String) {
            try {
                this.aiConfidence = Double.parseDouble(((String) v).replace("%", "").trim());
            } catch (Exception e) {
                this.aiConfidence = 0.0;
            }
        } else {
            this.aiConfidence = v;
        }
    }

    public String getAiProvider(){ return aiProvider; } public void setAiProvider(String v){ aiProvider = v; }
    public String getAiModel(){ return aiModel; } public void setAiModel(String v){ aiModel = v; }
    public Long getAiAnalyzedAt(){ return aiAnalyzedAt; } public void setAiAnalyzedAt(Long v){ aiAnalyzedAt = v; }
    public String getAiError(){ return aiError; } public void setAiError(String v){ aiError = v; }

    public String getInsight() { return insight; }
    public void setInsight(String v) { insight = v; }

    public Object getAccuracy() { return accuracy; }
    public void setAccuracy(Object v) { accuracy = v; }

    public Double getAccuracyValue() {
        if (accuracy == null) return null;
        if (accuracy instanceof Number) return ((Number) accuracy).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(accuracy).replace("%", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean hasAiResult() {
        return "completed".equals(aiStatus) && aiScore != null;
    }
}