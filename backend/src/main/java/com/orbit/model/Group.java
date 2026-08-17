package com.orbit.model;

public class Group {

    private String id;
    private String name;
    private long createdAt;


    // Required by Firebase
    public Group() {
    }


    public Group(
            String id,
            String name) {

        this.id = id;
        this.name = name;
        this.createdAt =
                System.currentTimeMillis();
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public long getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}