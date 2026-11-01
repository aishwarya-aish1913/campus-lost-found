package com.campus.lostfound.model;

import java.time.LocalDate;

public class LostItem extends Item {
    private String secretMark;

    public LostItem(int id, int userId, String itemName, String category, String location, LocalDate eventDate, String status, String secretMark) {
        super(id, userId, itemName, category, location, eventDate, status);
        this.secretMark = secretMark;
    }

    public String getSecretMark() { 
        return secretMark; 
    }

    @Override
    public String getSummary() {
        return "[LOST] " + getItemName() + " | Category: " + getCategory() + 
               " | Location: " + getLocation() + " | Date: " + getEventDate() + " | Status: " + getStatus();
    }
}