package com.campus.lostfound.model;

public class User {
    private int id;
    private String name;
    private String email;
    private int stars;

    public User(int id, String name, String email, int stars) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.stars = stars;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getStars() { return stars; }
    public void addStar() { this.stars++; }

    @Override
    public String toString() {
        return "Student: " + name + " (" + email + ") | Stars: ⭐ " + stars;
    }
}