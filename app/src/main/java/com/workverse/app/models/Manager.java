package com.workverse.app.models;

public class Manager {
    private String id;
    private String name;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String designation;
    private String campaign;
    private String userId;
    private String status;

    public Manager() {}

    public Manager(String id, String fullName, String username, String email, String phone, String designation, String campaign, String userId) {
        this.id = id;
        this.fullName = fullName;
        this.name = fullName;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.designation = designation;
        this.campaign = campaign;
        this.userId = userId;
        this.status = "active";
    }

    public String getId() { return id; }
    public void setId(String v) { this.id = v; }

    public String getName() { return name != null ? name : fullName; }
    public void setName(String v) { this.name = v; }

    public String getFullName() { return fullName; }
    public void setFullName(String v) { this.fullName = v; }

    public String getUsername() { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }

    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }

    public String getDesignation() { return designation; }
    public void setDesignation(String v) { this.designation = v; }

    public String getCampaign() { return campaign; }
    public void setCampaign(String v) { this.campaign = v; }

    public String getUserId() { return userId; }
    public void setUserId(String v) { this.userId = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}