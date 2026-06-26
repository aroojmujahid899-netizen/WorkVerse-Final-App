package com.workverse.app.models;
public class User {
    private String uid, username, email, fullName, phone, role, department, designation;
    public User() {}
    public User(String uid, String username, String email, String fullName, String phone, String role) {
        this.uid=uid; this.username=username; this.email=email;
        this.fullName=fullName; this.phone=phone; this.role=role;
    }
    public String getUid(){return uid;} public void setUid(String v){uid=v;}
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getRole(){return role;} public void setRole(String v){role=v;}
    public String getDepartment(){return department;} public void setDepartment(String v){department=v;}
    public String getDesignation(){return designation;} public void setDesignation(String v){designation=v;}
}