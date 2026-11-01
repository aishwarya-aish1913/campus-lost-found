package com.campus.lostfound;

import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.LostFoundService;
import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        LostFoundService service = new LostFoundService();

        System.out.println("==================================================");
        System.out.println("   CAMPUS LOST & FOUND SYSTEM - WORKFLOW RUNNER   ");
        System.out.println("==================================================\n");

        // Initial Profiles check (Ravi = ID 1, Priya = ID 2)
        User ravi = service.getUserProfile(1);
        User priya = service.getUserProfile(2);
        System.out.println("Initial State:");
        System.out.println(ravi);
        System.out.println(priya);
        System.out.println("--------------------------------------------------\n");

        // STEP 1: Ravi lost item report panraan
        System.out.println("[STEP 1] Ravi reporting Lost Water Bottle...");
        LostItem raviBottle = new LostItem(
                0, 1, "Water Bottle", "Bottle", "D Block", 
                LocalDate.of(2026, 10, 16), "Open", 
                "Dent at bottom, initials RK inside" // Secret mark kept safe
        );
        boolean lostSuccess = service.reportLost(raviBottle);
        System.out.println("Ravi's lost report saved: " + lostSuccess + "\n");

        // STEP 2: Priya bottle kandupidichu report panraanga
        System.out.println("[STEP 2] Priya reporting Found Water Bottle...");
        FoundItem priyaBottle = new FoundItem(
                0, 2, "Water Bottle", "Bottle", "D Block", 
                LocalDate.of(2026, 10, 16), "Open", 
                "uploads/blue_bottle_101.jpg", 
                "Admin Office", // Handed over to Admin Office
                "Admin Office la kudutthuten"
        );
        int generatedItemId = service.reportFound(priyaBottle);
        System.out.println("Priya's found item registered with Item ID: #" + generatedItemId + "\n");

        // STEP 3: Ravi search panraan (Filters: Bottle, D Block)
        System.out.println("[STEP 3] Ravi searching found items: Category = 'Bottle', Location = 'D Block'...");
        List<FoundItem> searchResults = service.searchItems("Bottle", "D Block");
        System.out.println("Found " + searchResults.size() + " matching item(s):");
        for (FoundItem item : searchResults) {
            System.out.println(" -> " + item.getSummary());
        }
        System.out.println();

        // STEP 4: Ravi bottle ah edukkuraan / Contact Request
        System.out.println("[STEP 4] Ravi initiates Claim / Contact Request for Item #" + generatedItemId + "...");
        service.requestContact(generatedItemId, 1, 2);
        System.out.println("Contact request recorded with status: 'Pending'\n");

        // STEP 5: Ravi bottle physical-ah verify panni "Received" confirm panraan
        System.out.println("[STEP 5] Ravi verifies secret mark and confirms 'Received'...");
        try {
            boolean isReturned = service.confirmReceived(
                    generatedItemId, 
                    1, 
                    "Dent at bottom, initials RK" // Offline proof verification
            );
            System.out.println("Receipt confirmed: " + isReturned + "\n");
        } catch (Exception e) {
            System.err.println("Verification Error: " + e.getMessage());
        }

        // STEP 6: Stars Check (Priya ku 1 Star add aaiducha nu verify panrom)
        System.out.println("[STEP 6] Checking Updated Profiles (MySQL Trigger Verification)...");
        User updatedRavi = service.getUserProfile(1);
        User updatedPriya = service.getUserProfile(2);
        System.out.println(updatedRavi);
        System.out.println(updatedPriya);

        System.out.println("\n==================================================");
        System.out.println("  SUCCESS: Entire Ravi & Priya lifecycle completed!");
        System.out.println("==================================================");
    }
}