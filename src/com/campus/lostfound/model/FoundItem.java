package com.campus.lostfound.model;

import java.time.LocalDate;

public class FoundItem extends Item implements Claimable {
    private String photoPath;
    private String holdingPlace;
    private String message;

    public FoundItem(int id, int userId, String itemName, String category, String location, LocalDate eventDate, String status, String photoPath, String holdingPlace, String message) {
        super(id, userId, itemName, category, location, eventDate, status);
        this.photoPath = photoPath;
        this.holdingPlace = holdingPlace;
        this.message = message;
    }

    public String getPhotoPath() { return photoPath; }
    public String getHoldingPlace() { return holdingPlace; }
    public String getMessage() { return message; }

    @Override
    public boolean verifySecretMark(String providedMark) {
        return providedMark != null && !providedMark.trim().isEmpty();
    }

    @Override
    public void confirmReceipt() {
        this.setStatus("Returned");
    }

    @Override
    public String getSummary() {
        return "[FOUND ITEM #" + getId() + "] " + getItemName() + " (" + getCategory() + ")" +
               " | Location: " + getLocation() + 
               " | Held At: " + holdingPlace + 
               " | Note: " + message;
    }
}