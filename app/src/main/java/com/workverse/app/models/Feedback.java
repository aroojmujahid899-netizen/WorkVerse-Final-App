package com.workverse.app.models;

public class Feedback {
    private String id, userId, fromUserName, title, message, role, sentiment;
    private long timestamp;

    public Feedback() {
    }

    public Feedback(String userId, String fromUserName, String title, String message, String role) {
        this.userId = userId;
        this.fromUserName = fromUserName;
        this.title = title;
        this.message = message;
        this.role = role;
        this.timestamp = System.currentTimeMillis();
        this.sentiment = "Neutral";
    }

    public String getId() {
        return id;
    }

    public void setId(String v) {
        id = v;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String v) {
        userId = v;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String v) {
        fromUserName = v;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String v) {
        message = v;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String v) {
        role = v;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String v) {
        sentiment = v;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long v) {
        timestamp = v;
    }
}
