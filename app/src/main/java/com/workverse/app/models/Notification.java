package com.workverse.app.models;
public class Notification {
    private String id, title, message, targetRole, senderName;
    private long timestamp;
    private boolean isRead;
    public Notification() {}
    public Notification(String title,String message,String targetRole,String senderName){
        this.title=title;this.message=message;this.targetRole=targetRole;
        this.senderName=senderName;this.timestamp=System.currentTimeMillis();this.isRead=false;
    }
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getTargetRole(){return targetRole;} public void setTargetRole(String v){targetRole=v;}
    public String getSenderName(){return senderName;} public void setSenderName(String v){senderName=v;}
    public long getTimestamp(){return timestamp;} public void setTimestamp(long v){timestamp=v;}
    public boolean isRead(){return isRead;} public void setRead(boolean v){isRead=v;}
}