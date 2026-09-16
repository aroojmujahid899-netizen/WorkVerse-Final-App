package com.workverse.app.models;

public class Notification {
    private String id, title, message, targetRole, senderName;
    private String targetDesignation; // "All", "Fronters", "Verifiers", "Closers" - null/empty if not applicable
    private String targetUserId;      // set when notification is meant for ONE specific employee only
    private long timestamp;
    private boolean isRead;

    public Notification() {}

    public Notification(String title, String message, String targetRole, String senderName) {
        this.title = title; this.message = message; this.targetRole = targetRole;
        this.senderName = senderName; this.timestamp = System.currentTimeMillis(); this.isRead = false;
    }

    public Notification(String title, String message, String targetRole, String senderName,
                        String targetDesignation, String targetUserId) {
        this.title = title; this.message = message; this.targetRole = targetRole;
        this.senderName = senderName; this.timestamp = System.currentTimeMillis(); this.isRead = false;
        this.targetDesignation = targetDesignation; this.targetUserId = targetUserId;
    }

    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getTargetRole(){return targetRole;} public void setTargetRole(String v){targetRole=v;}
    public String getSenderName(){return senderName;} public void setSenderName(String v){senderName=v;}
    public long getTimestamp(){return timestamp;} public void setTimestamp(long v){timestamp=v;}
    public boolean isRead(){return isRead;} public void setRead(boolean v){isRead=v;}
    public String getTargetDesignation(){return targetDesignation;} public void setTargetDesignation(String v){targetDesignation=v;}
    public String getTargetUserId(){return targetUserId;} public void setTargetUserId(String v){targetUserId=v;}
}