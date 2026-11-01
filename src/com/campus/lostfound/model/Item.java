package com.campus.lostfound.model;

import java.time.LocalDate;

public abstract class Item {
    private int id;
    private int userId;
    private String itemName;
    private String category;
    private String location;
    private LocalDate eventDate;
    private String status;

    public Item(int id, int userId, String itemName, String category, String location, LocalDate eventDate, String status) {
        this.id = id;
        this.userId = userId;
        this.itemName = itemName;
        this.category = category;
        this.location = location;
        this.eventDate = eventDate;
        this.status = status;
    }

    public abstract String getSummary();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }
    public LocalDate getEventDate() { return eventDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}