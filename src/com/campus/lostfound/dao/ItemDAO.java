package com.campus.lostfound.dao;

import com.campus.lostfound.model.FoundItem;
import com.campus.lostfound.model.LostItem;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    // 1. Ravi Lost Item Report panraan (PreparedStatement - SQL Injection protection)
    public boolean insertLostItem(LostItem item) {
        String sql = "INSERT INTO items (user_id, item_name, category, item_type, location, event_date, public_description, secret_mark, status) " +
                     "VALUES (?, ?, ?, 'LOST', ?, ?, ?, ?, 'Open')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getLocation());
            ps.setDate(5, Date.valueOf(item.getEventDate()));
            ps.setString(6, item.getSummary());
            ps.setString(7, item.getSecretMark());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error reporting lost item: " + e.getMessage());
            return false;
        }
    }

    // 2. Priya Found Item Report panraanga (Returns auto-generated item id)
    public int insertFoundItem(FoundItem item) {
        String sql = "INSERT INTO items (user_id, item_name, category, item_type, location, event_date, public_description, photo_path, holding_place, status) " +
                     "VALUES (?, ?, ?, 'FOUND', ?, ?, ?, ?, ?, 'Open')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setString(4, item.getLocation());
            ps.setDate(5, Date.valueOf(item.getEventDate()));
            ps.setString(6, item.getMessage());
            ps.setString(7, item.getPhotoPath());
            ps.setString(8, item.getHoldingPlace());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reporting found item: " + e.getMessage());
        }
        return -1;
    }

    // 3. Search Engine: Public View use panni active items fetch panrom (Collection List)
    public List<FoundItem> getAllOpenFoundItems() {
        List<FoundItem> list = new ArrayList<>();
        String sql = "SELECT * FROM vw_public_found_items";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FoundItem item = new FoundItem(
                    rs.getInt("id"),
                    0, // User ID hidden in public view
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("location"),
                    rs.getDate("event_date").toLocalDate(),
                    "Open",
                    rs.getString("photo_path"),
                    rs.getString("holding_place"),
                    rs.getString("public_description")
                );
                list.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching found items: " + e.getMessage());
        }
        return list;
    }

    // 4. Contact Request create panradhu
    public boolean createContactRequest(int itemId, int requesterId, int finderId) {
        String sql = "INSERT INTO contact_requests (item_id, requester_id, finder_id, status) VALUES (?, ?, ?, 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, itemId);
            ps.setInt(2, requesterId);
            ps.setInt(3, finderId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating contact request: " + e.getMessage());
            return false;
        }
    }

    // 5. ACID Transaction (DBMS Unit IV): Ravi confirms received -> Triggers Star reward for Priya
    public boolean confirmItemReceived(int itemId, int requesterId) {
        Connection conn = null;
        String updateItemSQL = "UPDATE items SET status = 'Returned' WHERE id = ?";
        String updateRequestSQL = "UPDATE contact_requests SET status = 'Accepted' WHERE item_id = ? AND requester_id = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // ACID Transaction: Begin

            // Operation 1: Item status 'Returned' aagum (MySQL Trigger fires and adds 1 star to Priya)
            try (PreparedStatement psItem = conn.prepareStatement(updateItemSQL)) {
                psItem.setInt(1, itemId);
                psItem.executeUpdate();
            }

            // Operation 2: Contact request close aagum
            try (PreparedStatement psReq = conn.prepareStatement(updateRequestSQL)) {
                psReq.setInt(1, itemId);
                psReq.setInt(2, requesterId);
                psReq.executeUpdate();
            }

            conn.commit(); // Transaction Success: Atomicity & Consistency guaranteed!
            System.out.println(" Transaction Committed: Item successfully returned.");
            return true;

        } catch (SQLException e) {
            System.err.println(" Transaction Failed! Rolling back changes: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on any failure
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    System.err.println("Close error: " + ex.getMessage());
                }
            }
        }
    }

    // 6. User profile & Stars check panna
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("stars")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
}