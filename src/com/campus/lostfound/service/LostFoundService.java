package com.campus.lostfound.service;

import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.exception.ItemNotFoundException;
import com.campus.lostfound.exception.UnauthorizedClaimException;
import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class LostFoundService {
    private ItemDAO itemDAO;

    public LostFoundService() {
        this.itemDAO = new ItemDAO();
    }

    // Step 1: Ravi reports lost item
    public boolean reportLost(LostItem item) {
        return itemDAO.insertLostItem(item);
    }

    // Step 2: Priya reports found item
    public int reportFound(FoundItem item) {
        return itemDAO.insertFoundItem(item);
    }

    // Step 3: Ravi searches items (Streams API & Lambda Expressions - OOPs Unit V)
    public List<FoundItem> searchItems(String category, String location) {
        List<FoundItem> allItems = itemDAO.getAllOpenFoundItems();

        return allItems.stream()
                .filter(item -> category == null || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> location == null || item.getLocation().equalsIgnoreCase(location))
                .sorted((item1, item2) -> item2.getEventDate().compareTo(item1.getEventDate())) // Latest first
                .collect(Collectors.toList());
    }

    // Step 4: Ravi initiates contact request
    public boolean requestContact(int itemId, int requesterId, int finderId) {
        return itemDAO.createContactRequest(itemId, requesterId, finderId);
    }

    // Step 5 & 6: Ravi confirms "Received" -> Trigger fires -> Star awarded to Priya
    public boolean confirmReceived(int itemId, int requesterId, String secretMarkProof) 
            throws UnauthorizedClaimException, ItemNotFoundException {
        
        // Safety verification: Secret mark empty-ah irukka koodadhu
        if (secretMarkProof == null || secretMarkProof.trim().isEmpty()) {
            throw new UnauthorizedClaimException("Secret mark proof cannot be empty during physical handover!");
        }

        boolean success = itemDAO.confirmItemReceived(itemId, requesterId);
        if (!success) {
            throw new ItemNotFoundException("Item ID #" + itemId + " not found or already returned.");
        }
        return true;
    }

    public User getUserProfile(int userId) {
        return itemDAO.getUserById(userId);
    }
}