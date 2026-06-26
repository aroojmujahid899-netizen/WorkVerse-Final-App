package com.workverse.app.models;
public class Role {
    private String id, name, description;
    public Role() {}
    public Role(String name, String description){this.name=name;this.description=description;}
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
}