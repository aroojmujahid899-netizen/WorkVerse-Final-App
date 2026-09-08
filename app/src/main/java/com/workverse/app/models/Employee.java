package com.workverse.app.models;

public class Employee {
    private String id, name, email, phone, designation, department, campaign, userId, status;

    public Employee() {}

    public Employee(String id, String name, String email, String phone, String designation, String department, String userId) {
        this.id = id; this.name = name; this.email = email; this.phone = phone;
        this.designation = designation; this.department = department; this.userId = userId; this.status = "active";
    }

    public String getId() { return id; } public void setId(String v) { id = v; }
    public String getName() { return name; } public void setName(String v) { name = v; }
    public String getEmail() { return email; } public void setEmail(String v) { email = v; }
    public String getPhone() { return phone; } public void setPhone(String v) { phone = v; }
    public String getDesignation() { return designation; } public void setDesignation(String v) { designation = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { department = v; }
    public String getCampaign() { return campaign; } public void setCampaign(String v) { campaign = v; }
    public String getUserId() { return userId; } public void setUserId(String v) { userId = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
}