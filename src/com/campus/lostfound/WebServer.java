package com.campus.lostfound;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class WebServer {
    private static final int PORT = 8080;
    private static final String DB_URL = "jdbc:mysql://mysql-lost-found-campus-lost-found-semproject.c.aivencloud.com:27920/defaultdb?sslMode=REQUIRED&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "avnadmin";
    private static final String DB_PASS = "AVNS_-TMYHfepnUuHQ7iK5yJ";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
    public static void main(String[] args) throws IOException {
        initDatabase();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/register", new RegisterHandler());
        server.createContext("/api/report", new ReportItemHandler());
        server.createContext("/api/items", new GetItemsHandler());
        server.createContext("/api/claim", new ClaimHandler());
        server.createContext("/api/requests", new RequestManageHandler());
        server.createContext("/api/star", new StarAppreciateHandler());
        server.createContext("/api/notifications", new NotificationHandler());

        server.setExecutor(null);
        System.out.println("=================================================");
        System.out.println("🚀 Campus Lost & Found Running: http://localhost:" + PORT);
        System.out.println("=================================================");
        server.start();
    }

    private static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(120) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "stars INT DEFAULT 0)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT, " +
                    "item_name VARCHAR(120) NOT NULL, " +
                    "item_type VARCHAR(20) NOT NULL, " +
                    "category VARCHAR(60), " +
                    "location VARCHAR(150), " +
                    "holder_name VARCHAR(100), " +
                    "description TEXT, " +
                    "image_data LONGTEXT, " +
                    "status VARCHAR(30) DEFAULT 'OPEN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS claim_requests (" +
                    "request_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "item_id INT NOT NULL, " +
                    "loser_id INT NOT NULL, " +
                    "finder_id INT NOT NULL, " +
                    "secret_mark TEXT NOT NULL, " +
                    "handover_place VARCHAR(150), " +
                    "status VARCHAR(30) DEFAULT 'PENDING', " +
                    "is_appreciated BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notifications (" +
                    "notif_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            try { stmt.executeUpdate("ALTER TABLE users ADD COLUMN stars INT DEFAULT 0"); } catch (Exception ignored) {}
            try { stmt.executeUpdate("ALTER TABLE claim_requests ADD COLUMN is_appreciated BOOLEAN DEFAULT FALSE"); } catch (Exception ignored) {}
        } catch (Exception e) {
            System.err.println("DB Notice: " + e.getMessage());
        }
    }

    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getIndexHtml();
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }

    private static String getIndexHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "  <meta charset='UTF-8'>\n" +
                "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "  <title>Smart Campus Lost & Found System</title>\n" +
                "  <link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap' rel='stylesheet'>\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --primary: #6366f1; --primary-dark: #4f46e5;\n" +
                "      --secondary: #06b6d4; --accent-star: #f59e0b;\n" +
                "      --danger: #ef4444; --success: #10b981;\n" +
                "      --card-bg: #ffffff; --bg-light: #f8fafc;\n" +
                "    }\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }\n" +
                "    body { background: var(--bg-light); color: #1e293b; min-height: 100vh; }\n" +
                "    nav { background: #0f172a; padding: 16px 36px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 4px 20px rgba(0,0,0,0.12); position: sticky; top: 0; z-index: 100; }\n" +
                "    .logo { font-size: 21px; font-weight: 800; color: #fff; display: flex; align-items: center; gap: 8px; cursor: pointer; }\n" +
                "    .logo span { background: linear-gradient(135deg, #a5b4fc, #38bdf8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }\n" +
                "    .btn { padding: 10px 20px; border-radius: 10px; border: none; font-weight: 700; font-size: 14px; cursor: pointer; transition: all 0.25s ease; display: inline-flex; align-items: center; justify-content: center; gap: 6px; }\n" +
                "    .btn-bright { background: linear-gradient(135deg, #6366f1, #06b6d4); color: white; box-shadow: 0 4px 15px rgba(99, 102, 241, 0.4); }\n" +
                "    .btn-bright:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(99, 102, 241, 0.5); }\n" +
                "    .btn-star { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.35); }\n" +
                "    .btn-star:hover { transform: scale(1.03); }\n" +
                "    .btn-danger { background: #fee2e2; color: var(--danger); }\n" +
                "    .btn-danger:hover { background: #fecaca; }\n" +
                "    .btn-success { background: #dcfce7; color: #15803d; }\n" +
                "    .btn-success:hover { background: #bbf7d0; }\n" +
                "    .container { max-width: 1200px; margin: 30px auto; padding: 0 24px; }\n" +
                "    /* Landing Screen */\n" +
                "    #landingView { text-align: center; padding: 70px 20px; }\n" +
                "    .badge-hero { display: inline-block; padding: 6px 18px; background: #e0e7ff; color: #4338ca; border-radius: 30px; font-size: 13px; font-weight: 800; margin-bottom: 20px; text-transform: uppercase; letter-spacing: 0.5px; }\n" +
                "    #landingView h1 { font-size: 48px; font-weight: 800; color: #0f172a; line-height: 1.25; margin-bottom: 18px; }\n" +
                "    #landingView p { font-size: 17px; color: #64748b; max-width: 620px; margin: 0 auto 35px auto; line-height: 1.6; }\n" +
                "    /* Auth Modal / Card */\n" +
                "    #authBox { max-width: 420px; margin: 40px auto; background: white; padding: 36px; border-radius: 20px; box-shadow: 0 12px 35px rgba(0,0,0,0.06); border: 1px solid #e2e8f0; }\n" +
                "    .form-group { margin-bottom: 16px; text-align: left; }\n" +
                "    .form-group label { display: block; font-size: 13px; font-weight: 700; color: #334155; margin-bottom: 6px; }\n" +
                "    .form-group input, .form-group select, .form-group textarea { width: 100%; padding: 12px 16px; border: 1.5px solid #cbd5e1; border-radius: 10px; font-size: 14px; outline: none; transition: border-color 0.2s; }\n" +
                "    .form-group input:focus, .form-group textarea:focus { border-color: var(--primary); box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15); }\n" +
                "    /* Dashboard */\n" +
                "    .user-profile-bar { display: flex; align-items: center; gap: 12px; background: white; padding: 12px 20px; border-radius: 12px; border: 1px solid #e2e8f0; margin-bottom: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }\n" +
                "    .star-pill { background: #fffbeb; color: #b45309; border: 1px solid #fef3c7; padding: 5px 14px; border-radius: 20px; font-weight: 800; font-size: 13px; display: inline-flex; align-items: center; gap: 6px; }\n" +
                "    .action-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 30px; }\n" +
                "    .action-card { padding: 28px; border-radius: 18px; color: white; cursor: pointer; transition: transform 0.2s, box-shadow 0.2s; position: relative; overflow: hidden; }\n" +
                "    .action-card:hover { transform: translateY(-4px); box-shadow: 0 10px 25px rgba(0,0,0,0.12); }\n" +
                "    .card-lost { background: linear-gradient(135deg, #f43f5e, #e11d48); }\n" +
                "    .card-found { background: linear-gradient(135deg, #10b981, #059669); }\n" +
                "    .alert-banner { background: #fffbeb; border-left: 5px solid #f59e0b; padding: 15px 20px; border-radius: 12px; margin-bottom: 24px; display: none; color: #92400e; font-weight: 600; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.08); }\n" +
                "    .search-bar { width: 100%; padding: 16px 22px; border-radius: 14px; border: 1.5px solid #e2e8f0; font-size: 16px; background: white; box-shadow: 0 4px 15px rgba(0,0,0,0.03); outline: none; margin-bottom: 30px; }\n" +
                "    /* Item Feed Grid */\n" +
                "    .items-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(290px, 1fr)); gap: 24px; }\n" +
                "    .item-card { background: white; border-radius: 16px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 20px rgba(0,0,0,0.04); display: flex; flex-direction: column; transition: transform 0.2s; }\n" +
                "    .item-card:hover { transform: translateY(-3px); box-shadow: 0 10px 25px rgba(0,0,0,0.08); }\n" +
                "    .item-card img { width: 100%; height: 210px; object-fit: cover; background: #f1f5f9; }\n" +
                "    .item-body { padding: 18px; flex: 1; display: flex; flex-direction: column; justify-content: space-between; }\n" +
                "    .holder-badge { background: #f8fafc; border: 1.5px dashed #cbd5e1; border-radius: 10px; padding: 12px; margin: 12px 0; font-size: 13px; line-height: 1.5; }\n" +
                "    .modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(5px); display: none; justify-content: center; align-items: center; z-index: 1000; }\n" +
                "    .modal-box { background: white; width: 92%; max-width: 520px; padding: 32px; border-radius: 20px; box-shadow: 0 25px 50px rgba(0,0,0,0.25); max-height: 90vh; overflow-y: auto; }\n" +
                "    .req-item { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; margin-bottom: 14px; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "  <nav>\n" +
                "    <div class='logo' onclick='goHome()'>🛡️ Campus <span>Lost & Found</span></div>\n" +
                "    <div id='navActions'>\n" +
                "      <button class='btn btn-bright' onclick='showLogin()'>Sign In</button>\n" +
                "    </div>\n" +
                "  </nav>\n" +
                "\n" +
                "  <div class='container'>\n" +
                "    <!-- 1. LANDING PAGE -->\n" +
                "    <div id='landingView'>\n" +
                "      <span class='badge-hero'>✨ Campus Integrity & Recovery</span>\n" +
                "      <h1>Lost Something? Found an Item?<br>Recover Safely with Secret Marks</h1>\n" +
                "      <p>Only finders upload photos. True owners unlock their belongings by describing hidden identification marks. Award stars to honest finders!</p>\n" +
                "      <button class='btn btn-bright' style='font-size: 16px; padding: 15px 36px;' onclick='showLogin()'>Get Started 🚀</button>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- 2. AUTH VIEW -->\n" +
                "    <div id='authView' style='display:none;'>\n" +
                "      <div id='authBox'>\n" +
                "        <h2 id='authTitle' style='margin-bottom: 20px; font-weight: 800; text-align: center;'>Sign In</h2>\n" +
                "        <div class='form-group' id='nameGroup' style='display:none;'>\n" +
                "          <label>Full Name</label>\n" +
                "          <input type='text' id='userName' placeholder='e.g., Aishwarya'>\n" +
                "        </div>\n" +
                "        <div class='form-group'>\n" +
                "          <label>College Email</label>\n" +
                "          <input type='email' id='userEmail' placeholder='yourname@campus.edu'>\n" +
                "        </div>\n" +
                "        <div class='form-group'>\n" +
                "          <label>Password</label>\n" +
                "          <input type='password' id='userPassword' placeholder='••••••••'>\n" +
                "        </div>\n" +
                "        <button class='btn btn-bright' style='width: 100%; padding: 14px; margin-top: 6px;' id='authBtn' onclick='handleAuth()'>Sign In</button>\n" +
                "        <p style='text-align: center; margin-top: 18px; font-size: 14px;'>\n" +
                "          <a href='javascript:void(0)' id='switchAuth' onclick='toggleAuthMode()' style='color: var(--primary); text-decoration: none; font-weight: 700;'>New user? Register here</a>\n" +
                "        </p>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- 3. DASHBOARD -->\n" +
                "    <div id='dashboardView' style='display:none;'>\n" +
                "      <div class='user-profile-bar'>\n" +
                "        <div style='flex: 1;'>\n" +
                "          <h3 id='dashGreeting' style='font-size: 17px;'>Welcome back!</h3>\n" +
                "          <p style='font-size: 13px; color: #64748b;'>Campus Recovery Dashboard</p>\n" +
                "        </div>\n" +
                "        <div class='star-pill' id='userStarBadge'>⭐ 0 Honor Stars</div>\n" +
                "      </div>\n" +
                "\n" +
                "      <div id='notifBanner' class='alert-banner'></div>\n" +
                "\n" +
                "      <div class='action-grid'>\n" +
                "        <div class='action-card card-lost' onclick='openReportModal(\"LOST\")'>\n" +
                "          <h2>📢 Report Lost Item</h2>\n" +
                "          <p style='margin-top: 6px; opacity: 0.9;'>Misplaced something? Add details (No photo required)</p>\n" +
                "        </div>\n" +
                "        <div class='action-card card-found' onclick='openReportModal(\"FOUND\")'>\n" +
                "          <h2>📷 Report Found Item</h2>\n" +
                "          <p style='margin-top: 6px; opacity: 0.9;'>Found something? Upload photo & pickup location</p>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "\n" +
                "      <div style='display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap;'>\n" +
                "        <button class='btn btn-success' onclick='openMyRequestsModal()'>📩 Review Claim Requests For My Found Items</button>\n" +
                "        <button class='btn btn-bright' onclick='openMyClaimsStatusModal()'>🔍 My Submitted Claims Status</button>\n" +
                "      </div>\n" +
                "\n" +
                "      <input type='text' id='searchInput' class='search-bar' placeholder='🔍 Search items by name...' onkeyup='filterItems()'>\n" +
                "\n" +
                "      <h3 style='margin-bottom: 18px; font-weight: 800;'>Campus Feed (Found Items with Photos)</h3>\n" +
                "      <div id='itemsGrid' class='items-grid'></div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- REPORT MODAL -->\n" +
                "  <div class='modal-overlay' id='reportModal'>\n" +
                "    <div class='modal-box'>\n" +
                "      <h3 id='modalTitle' style='margin-bottom: 18px;'>Report Item</h3>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Item Name *</label>\n" +
                "        <input type='text' id='modalItemName' placeholder='e.g., Fastrack Leather Watch, Blue Bottle'>\n" +
                "      </div>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Category</label>\n" +
                "        <select id='modalCategory'>\n" +
                "          <option value='Electronics'>Electronics</option>\n" +
                "          <option value='ID Card / Wallet'>ID Card / Wallet</option>\n" +
                "          <option value='Books & Notes'>Books & Notes</option>\n" +
                "          <option value='Accessories'>Accessories</option>\n" +
                "          <option value='Other'>Other</option>\n" +
                "        </select>\n" +
                "      </div>\n" +
                "      <div id='finderFields' style='display:none;'>\n" +
                "        <div class='form-group'>\n" +
                "          <label>Item With Whom? (Your Name/Dept) *</label>\n" +
                "          <input type='text' id='modalHolderName' placeholder='e.g., Priya - CSE 3rd Year'>\n" +
                "        </div>\n" +
                "        <div class='form-group'>\n" +
                "          <label>Where Was It Found? *</label>\n" +
                "          <input type='text' id='modalLocation' placeholder='e.g., Library 2nd Floor or Canteen'>\n" +
                "        </div>\n" +
                "        <div class='form-group'>\n" +
                "          <label>Upload Item Photo *</label>\n" +
                "          <input type='file' id='modalPhoto' accept='image/*'>\n" +
                "        </div>\n" +
                "      </div>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Notes / Description</label>\n" +
                "        <textarea id='modalDesc' rows='3' placeholder='Any visible details...'></textarea>\n" +
                "      </div>\n" +
                "      <div style='display: flex; gap: 10px; margin-top: 20px;'>\n" +
                "        <button class='btn btn-bright' style='flex: 1;' onclick='submitReport()'>Submit</button>\n" +
                "        <button class='btn btn-danger' style='flex: 1;' onclick='closeModal(\"reportModal\")'>Cancel</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- CLAIM ITEM MODAL (SECRET MARK) -->\n" +
                "  <div class='modal-overlay' id='claimModal'>\n" +
                "    <div class='modal-box'>\n" +
                "      <h3 style='margin-bottom: 10px;'>Verify Ownership</h3>\n" +
                "      <p style='font-size: 13.5px; color: #64748b; margin-bottom: 18px;'>Enter a <b>secret identification mark</b> (e.g., small scratch at the back, name sticker, wallpaper detail) that only the real owner knows.</p>\n" +
                "      <input type='hidden' id='claimItemId'>\n" +
                "      <input type='hidden' id='claimFinderId'>\n" +
                "      <div class='form-group'>\n" +
                "        <label>Secret Identification Mark *</label>\n" +
                "        <textarea id='claimSecretMark' rows='4' placeholder='Tell unique marks only you know...'></textarea>\n" +
                "      </div>\n" +
                "      <div style='display: flex; gap: 10px;'>\n" +
                "        <button class='btn btn-bright' style='flex: 1;' onclick='submitClaim()'>Send Verification Request</button>\n" +
                "        <button class='btn btn-danger' style='flex: 1;' onclick='closeModal(\"claimModal\")'>Cancel</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- FINDER REQUEST MANAGEMENT MODAL -->\n" +
                "  <div class='modal-overlay' id='requestsModal'>\n" +
                "    <div class='modal-box' style='max-width: 620px;'>\n" +
                "      <h3 style='margin-bottom: 18px;'>Incoming Claim Requests</h3>\n" +
                "      <div id='requestsList'></div>\n" +
                "      <button class='btn btn-danger' style='width: 100%; margin-top: 15px;' onclick='closeModal(\"requestsModal\")'>Close</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- MY CLAIMS STATUS & APPRECIATE MODAL -->\n" +
                "  <div class='modal-overlay' id='statusModal'>\n" +
                "    <div class='modal-box' style='max-width: 620px;'>\n" +
                "      <h3 style='margin-bottom: 18px;'>My Claim Requests & Appreciate Finder</h3>\n" +
                "      <div id='statusList'></div>\n" +
                "      <button class='btn btn-danger' style='width: 100%; margin-top: 15px;' onclick='closeModal(\"statusModal\")'>Close</button>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <script>\n" +
                "    let currentUser = null;\n" +
                "    let isRegisterMode = false;\n" +
                "    let currentType = 'LOST';\n" +
                "\n" +
                "    function goHome() {\n" +
                "      if (currentUser) showDashboard();\n" +
                "      else {\n" +
                "        document.getElementById('landingView').style.display = 'block';\n" +
                "        document.getElementById('authView').style.display = 'none';\n" +
                "        document.getElementById('dashboardView').style.display = 'none';\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function showLogin() {\n" +
                "      document.getElementById('landingView').style.display = 'none';\n" +
                "      document.getElementById('authView').style.display = 'block';\n" +
                "      document.getElementById('dashboardView').style.display = 'none';\n" +
                "    }\n" +
                "\n" +
                "    function toggleAuthMode() {\n" +
                "      isRegisterMode = !isRegisterMode;\n" +
                "      document.getElementById('authTitle').innerText = isRegisterMode ? 'Create Account' : 'Sign In';\n" +
                "      document.getElementById('nameGroup').style.display = isRegisterMode ? 'block' : 'none';\n" +
                "      document.getElementById('authBtn').innerText = isRegisterMode ? 'Register' : 'Sign In';\n" +
                "      document.getElementById('switchAuth').innerText = isRegisterMode ? 'Already have an account? Sign In' : 'New user? Register here';\n" +
                "    }\n" +
                "\n" +
                "    async function handleAuth() {\n" +
                "      const email = document.getElementById('userEmail').value.trim();\n" +
                "      const password = document.getElementById('userPassword').value;\n" +
                "      const name = document.getElementById('userName').value.trim();\n" +
                "\n" +
                "      if (!email || !password || (isRegisterMode && !name)) { alert('Please fill in required fields'); return; }\n" +
                "\n" +
                "      const url = isRegisterMode ? '/api/register' : '/api/login';\n" +
                "      const payload = isRegisterMode ? { name, email, password } : { email, password };\n" +
                "\n" +
                "      const res = await fetch(url, { method: 'POST', body: JSON.stringify(payload) });\n" +
                "      const data = await res.json();\n" +
                "      if (data.success) {\n" +
                "        currentUser = data.user;\n" +
                "        showDashboard();\n" +
                "      } else {\n" +
                "        alert(data.message || 'Authentication error. Please check credentials or register.');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function showDashboard() {\n" +
                "      document.getElementById('landingView').style.display = 'none';\n" +
                "      document.getElementById('authView').style.display = 'none';\n" +
                "      document.getElementById('dashboardView').style.display = 'block';\n" +
                "      document.getElementById('dashGreeting').innerText = `Hello, ${currentUser.name}! 👋`;\n" +
                "      document.getElementById('userStarBadge').innerText = `⭐ ${currentUser.stars || 0} Honor Stars`;\n" +
                "      document.getElementById('navActions').innerHTML = `<span style='color:white; font-weight:600; margin-right:15px;'>${currentUser.name}</span> <button class='btn btn-danger' onclick='logout()'>Logout</button>`;\n" +
                "      loadItems();\n" +
                "      checkNotifications();\n" +
                "    }\n" +
                "\n" +
                "    function logout() {\n" +
                "      currentUser = null;\n" +
                "      document.getElementById('navActions').innerHTML = `<button class='btn btn-bright' onclick='showLogin()'>Sign In</button>`;\n" +
                "      goHome();\n" +
                "    }\n" +
                "\n" +
                "    function openReportModal(type) {\n" +
                "      currentType = type;\n" +
                "      document.getElementById('modalTitle').innerText = type === 'LOST' ? '📢 Report Lost Item (No Photo Needed)' : '📷 Report Found Item with Photo';\n" +
                "      document.getElementById('finderFields').style.display = type === 'FOUND' ? 'block' : 'none';\n" +
                "      document.getElementById('reportModal').style.display = 'flex';\n" +
                "    }\n" +
                "\n" +
                "    function closeModal(id) { document.getElementById(id).style.display = 'none'; }\n" +
                "\n" +
                "    const toBase64 = file => new Promise((resolve, reject) => {\n" +
                "      const reader = new FileReader();\n" +
                "      reader.readAsDataURL(file);\n" +
                "      reader.onload = () => resolve(reader.result);\n" +
                "      reader.onerror = error => reject(error);\n" +
                "    });\n" +
                "\n" +
                "    async function submitReport() {\n" +
                "      const itemName = document.getElementById('modalItemName').value.trim();\n" +
                "      const category = document.getElementById('modalCategory').value;\n" +
                "      const desc = document.getElementById('modalDesc').value.trim();\n" +
                "\n" +
                "      if (!itemName) { alert('Item Name is required'); return; }\n" +
                "\n" +
                "      let holderName = '', location = '', imageData = '';\n" +
                "      if (currentType === 'FOUND') {\n" +
                "        holderName = document.getElementById('modalHolderName').value.trim();\n" +
                "        location = document.getElementById('modalLocation').value.trim();\n" +
                "        const photoInput = document.getElementById('modalPhoto');\n" +
                "        if (!holderName || !location || photoInput.files.length === 0) {\n" +
                "          alert('Finder must enter Holder Name, Location, and Upload Photo'); return;\n" +
                "        }\n" +
                "        imageData = await toBase64(photoInput.files[0]);\n" +
                "      }\n" +
                "\n" +
                "      const payload = { userId: currentUser.userId, itemName, itemType: currentType, category, description: desc, holderName, location, imageData };\n" +
                "      const res = await fetch('/api/report', { method: 'POST', body: JSON.stringify(payload) });\n" +
                "      const data = await res.json();\n" +
                "      if (data.success) {\n" +
                "        alert('Item successfully reported!');\n" +
                "        closeModal('reportModal');\n" +
                "        loadItems();\n" +
                "        checkNotifications();\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function loadItems() {\n" +
                "      const res = await fetch('/api/items');\n" +
                "      const items = await res.json();\n" +
                "      renderCards(items);\n" +
                "    }\n" +
                "\n" +
                "    function renderCards(items) {\n" +
                "      const grid = document.getElementById('itemsGrid');\n" +
                "      if (!items || items.length === 0) {\n" +
                "        grid.innerHTML = '<p style=\"color:#64748b; grid-column:1/-1; text-align:center; padding:30px;\">No found items posted yet.</p>';\n" +
                "        return;\n" +
                "      }\n" +
                "      grid.innerHTML = items.map(item => `\n" +
                "        <div class='item-card' data-name='${item.itemName.toLowerCase()}'>\n" +
                "          <img src='${item.imageData}' alt='Found Item'>\n" +
                "          <div class='item-body'>\n" +
                "            <div>\n" +
                "              <div style='display:flex; justify-content:space-between; align-items:center;'>\n" +
                "                <span style='background:#dcfce7; color:#15803d; font-size:11px; font-weight:800; padding:4px 10px; border-radius:6px;'>🟢 FOUND</span>\n" +
                "                <span style='font-size:12px; color:#64748b; font-weight:600;'>${item.category}</span>\n" +
                "              </div>\n" +
                "              <h3 style='font-size:18px; margin:10px 0 4px 0; color:#0f172a;'>${item.itemName}</h3>\n" +
                "              <p style='font-size:13px; color:#475569;'>${item.description || 'No additional note.'}</p>\n" +
                "              <div class='holder-badge'>\n" +
                "                <p>📍 <b>Found At:</b> ${item.location}</p>\n" +
                "                <p>👤 <b>With:</b> ${item.holderName} <span style='color:#f59e0b; font-weight:700;'>(⭐ ${item.finderStars || 0})</span></p>\n" +
                "              </div>\n" +
                "            </div>\n" +
                "            <button class='btn btn-bright' style='width:100%; margin-top:8px;' onclick='openClaimModal(${item.itemId}, ${item.userId})'>✋ Claim (Send Secret Mark)</button>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "      `).join('');\n" +
                "    }\n" +
                "\n" +
                "    function filterItems() {\n" +
                "      const q = document.getElementById('searchInput').value.toLowerCase();\n" +
                "      document.querySelectorAll('.item-card').forEach(card => {\n" +
                "        card.style.display = card.getAttribute('data-name').includes(q) ? 'flex' : 'none';\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function openClaimModal(itemId, finderId) {\n" +
                "      if (currentUser.userId === finderId) { alert('You posted this item yourself!'); return; }\n" +
                "      document.getElementById('claimItemId').value = itemId;\n" +
                "      document.getElementById('claimFinderId').value = finderId;\n" +
                "      document.getElementById('claimSecretMark').value = '';\n" +
                "      document.getElementById('claimModal').style.display = 'flex';\n" +
                "    }\n" +
                "\n" +
                "    async function submitClaim() {\n" +
                "      const itemId = document.getElementById('claimItemId').value;\n" +
                "      const finderId = document.getElementById('claimFinderId').value;\n" +
                "      const secretMark = document.getElementById('claimSecretMark').value.trim();\n" +
                "      if (!secretMark) { alert('Please enter your secret mark to prove ownership'); return; }\n" +
                "\n" +
                "      const res = await fetch('/api/claim', { method: 'POST', body: JSON.stringify({ itemId, loserId: currentUser.userId, finderId, secretMark }) });\n" +
                "      const data = await res.json();\n" +
                "      if (data.success) {\n" +
                "        alert('Verification request sent to finder!');\n" +
                "        closeModal('claimModal');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function openMyRequestsModal() {\n" +
                "      const res = await fetch('/api/requests?type=finder&userId=' + currentUser.userId);\n" +
                "      const requests = await res.json();\n" +
                "      const container = document.getElementById('requestsList');\n" +
                "      if (!requests || requests.length === 0) {\n" +
                "        container.innerHTML = '<p style=\"color:#64748b; text-align:center; padding:15px;\">No pending claim requests right now.</p>';\n" +
                "      } else {\n" +
                "        container.innerHTML = requests.map(r => `\n" +
                "          <div class='req-item'>\n" +
                "            <p>📦 <b>Item:</b> ${r.itemName}</p>\n" +
                "            <p>👤 <b>Claimant:</b> ${r.loserName} (${r.loserEmail})</p>\n" +
                "            <p style='margin:8px 0; background:#fffbeb; padding:10px; border-radius:8px; color:#92400e;'>🔑 <b>Owner Secret Mark:</b> \"${r.secretMark}\"</p>\n" +
                "            <div style='display:flex; gap:8px; margin-top:10px;'>\n" +
                "              <input type='text' id='place_${r.requestId}' placeholder='Enter Handover Place & Time (e.g. Canteen 2 PM)' style='flex:1; padding:8px 12px; border:1px solid #cbd5e1; border-radius:8px;'>\n" +
                "              <button class='btn btn-success' onclick='respondClaim(${r.requestId}, \"ACCEPTED\", ${r.loserId})'>Accept & Meet</button>\n" +
                "              <button class='btn btn-danger' onclick='respondClaim(${r.requestId}, \"REJECTED\", ${r.loserId})'>Reject</button>\n" +
                "            </div>\n" +
                "          </div>\n" +
                "        `).join('');\n" +
                "      }\n" +
                "      document.getElementById('requestsModal').style.display = 'flex';\n" +
                "    }\n" +
                "\n" +
                "    async function openMyClaimsStatusModal() {\n" +
                "      const res = await fetch('/api/requests?type=loser&userId=' + currentUser.userId);\n" +
                "      const requests = await res.json();\n" +
                "      const container = document.getElementById('statusList');\n" +
                "      if (!requests || requests.length === 0) {\n" +
                "        container.innerHTML = '<p style=\"color:#64748b; text-align:center; padding:15px;\">You haven\\'t submitted any claims yet.</p>';\n" +
                "      } else {\n" +
                "        container.innerHTML = requests.map(r => `\n" +
                "          <div class='req-item'>\n" +
                "            <p>📦 <b>Item:</b> ${r.itemName}</p>\n" +
                "            <p>👤 <b>Finder:</b> ${r.finderName}</p>\n" +
                "            <p>📊 <b>Status:</b> <span style='font-weight:700; color:${r.status === 'ACCEPTED' ? '#15803d' : '#f59e0b'};'>${r.status}</span></p>\n" +
                "            ${r.status === 'ACCEPTED' ? `<p style='margin-top:6px; color:#15803d;'>📍 <b>Meet at:</b> ${r.handoverPlace}</p>` : ''}\n" +
                "            ${r.status === 'ACCEPTED' && !r.isAppreciated ? `\n" +
                "              <button class='btn btn-star' style='margin-top:10px;' onclick='appreciateFinder(${r.requestId}, ${r.finderId})'>⭐ Appreciate Finder with a Star</button>\n" +
                "            ` : (r.isAppreciated ? '<p style=\"margin-top:6px; color:#f59e0b; font-weight:700;\">⭐ You awarded a star for this!</p>' : '')}\n" +
                "          </div>\n" +
                "        `).join('');\n" +
                "      }\n" +
                "      document.getElementById('statusModal').style.display = 'flex';\n" +
                "    }\n" +
                "\n" +
                "    async function respondClaim(requestId, status, loserId) {\n" +
                "      let place = '';\n" +
                "      if (status === 'ACCEPTED') {\n" +
                "        place = document.getElementById('place_' + requestId).value.trim();\n" +
                "        if (!place) { alert('Please enter handover location/time'); return; }\n" +
                "      }\n" +
                "      const res = await fetch('/api/requests', { method: 'POST', body: JSON.stringify({ requestId, status, place, loserId }) });\n" +
                "      const data = await res.json();\n" +
                "      if (data.success) {\n" +
                "        alert('Response sent to owner!');\n" +
                "        openMyRequestsModal();\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function appreciateFinder(requestId, finderId) {\n" +
                "      const res = await fetch('/api/star', { method: 'POST', body: JSON.stringify({ requestId, finderId }) });\n" +
                "      const data = await res.json();\n" +
                "      if (data.success) {\n" +
                "        alert('⭐ Star awarded to the honest finder! Thank you.');\n" +
                "        openMyClaimsStatusModal();\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function checkNotifications() {\n" +
                "      if (!currentUser) return;\n" +
                "      const res = await fetch('/api/notifications?userId=' + currentUser.userId);\n" +
                "      const notifs = await res.json();\n" +
                "      const banner = document.getElementById('notifBanner');\n" +
                "      if (notifs && notifs.length > 0) {\n" +
                "        banner.style.display = 'block';\n" +
                "        banner.innerHTML = notifs.map(n => `<div>🔔 ${n.message}</div>`).join('');\n" +
                "      } else {\n" +
                "        banner.style.display = 'none';\n" +
                "      }\n" +
                "    }\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    // --- Backend Handlers ---

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT id AS user_id, name, email, COALESCE(stars, 0) AS stars FROM users WHERE email=? AND password=?")) {
                    stmt.setString(1, map.get("email") != null ? map.get("email").trim() : "");
                    stmt.setString(2, map.get("password") != null ? map.get("password").trim() : "");
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            sendResponse(exchange, 200, String.format("{\"success\":true,\"user\":{\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"stars\":%d}}",
                                    rs.getInt("user_id"), rs.getString("name"), rs.getString("email"), rs.getInt("stars")));
                            return;
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
                sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (name, email, password, stars) VALUES (?, ?, ?, 0)", Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, map.get("name"));
                    stmt.setString(2, map.get("email") != null ? map.get("email").trim() : "");
                    stmt.setString(3, map.get("password"));
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            sendResponse(exchange, 200, String.format("{\"success\":true,\"user\":{\"userId\":%d,\"name\":\"%s\",\"email\":\"%s\",\"stars\":0}}",
                                    rs.getInt(1), map.get("name"), map.get("email")));
                            return;
                        }
                    }
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Email already registered or database error\"}");
                }
            }
        }
    }

    static class ReportItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                int userId = Integer.parseInt(map.getOrDefault("userId", "0"));
                String itemName = map.get("itemName");
                String itemType = map.get("itemType");
                String category = map.get("category");
                String desc = map.get("description");
                String holderName = map.getOrDefault("holderName", "");
                String location = map.getOrDefault("location", "");
                String imageData = map.getOrDefault("imageData", "");

                try (Connection conn = getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO items (user_id, item_name, item_type, category, location, holder_name, description, image_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        stmt.setInt(1, userId);
                        stmt.setString(2, itemName);
                        stmt.setString(3, itemType);
                        stmt.setString(4, category);
                        stmt.setString(5, location);
                        stmt.setString(6, holderName);
                        stmt.setString(7, desc);
                        stmt.setString(8, imageData);
                        stmt.executeUpdate();
                    }

                    if ("FOUND".equalsIgnoreCase(itemType)) {
                        String matchSql = "SELECT user_id, item_name FROM items WHERE item_type='LOST' AND LOWER(item_name) LIKE ?";
                        try (PreparedStatement pLost = conn.prepareStatement(matchSql)) {
                            pLost.setString(1, "%" + itemName.toLowerCase().trim() + "%");
                            try (ResultSet rs = pLost.executeQuery()) {
                                while (rs.next()) {
                                    int loserId = rs.getInt("user_id");
                                    if (loserId != userId) {
                                        String msg = "Someone found an item matching your lost report '" + rs.getString("item_name") + "'. Check campus feed!";
                                        try (PreparedStatement nStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
                                            nStmt.setInt(1, loserId);
                                            nStmt.setString(2, msg);
                                            nStmt.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    sendResponse(exchange, 200, "{\"success\":true}");
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"success\":false}");
                }
            }
        }
    }

    static class GetItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT i.id AS item_id, i.user_id, i.item_name, i.category, i.location, i.holder_name, i.description, i.image_data, u.stars AS finder_stars " +
                         "FROM items i LEFT JOIN users u ON i.user_id = u.id " +
                         "WHERE i.item_type='FOUND' AND i.image_data IS NOT NULL AND i.image_data != '' ORDER BY i.id DESC")) {

                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{")
                            .append("\"itemId\":").append(rs.getInt("item_id")).append(",")
                            .append("\"userId\":").append(rs.getInt("user_id")).append(",")
                            .append("\"itemName\":\"").append(escapeJson(rs.getString("item_name"))).append("\",")
                            .append("\"category\":\"").append(escapeJson(rs.getString("category"))).append("\",")
                            .append("\"location\":\"").append(escapeJson(rs.getString("location"))).append("\",")
                            .append("\"holderName\":\"").append(escapeJson(rs.getString("holder_name"))).append("\",")
                            .append("\"description\":\"").append(escapeJson(rs.getString("description"))).append("\",")
                            .append("\"finderStars\":").append(rs.getInt("finder_stars")).append(",")
                            .append("\"imageData\":\"").append(rs.getString("image_data") != null ? rs.getString("image_data").replace("\r", "").replace("\n", "") : "").append("\"")
                            .append("}");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "[]"); }
        }
    }

    static class ClaimHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                try (Connection conn = getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO claim_requests (item_id, loser_id, finder_id, secret_mark) VALUES (?, ?, ?, ?)")) {
                    stmt.setInt(1, Integer.parseInt(map.get("itemId")));
                    stmt.setInt(2, Integer.parseInt(map.get("loserId")));
                    stmt.setInt(3, Integer.parseInt(map.get("finderId")));
                    stmt.setString(4, map.get("secretMark"));
                    stmt.executeUpdate();

                    try (PreparedStatement nStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
                        nStmt.setInt(1, Integer.parseInt(map.get("finderId")));
                        nStmt.setString(2, "New ownership verification request with secret mark received!");
                        nStmt.executeUpdate();
                    }
                    sendResponse(exchange, 200, "{\"success\":true}");
                } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "{\"success\":false}"); }
            }
        }
    }

    static class RequestManageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String type = query.contains("type=loser") ? "loser" : "finder";
                int userId = Integer.parseInt(query.split("userId=")[1].split("&")[0]);

                try (Connection conn = getConnection()) {
                    String sql = type.equals("finder")
                            ? "SELECT r.request_id, r.item_id, r.loser_id, r.secret_mark, r.status, i.item_name, u.name AS loser_name, u.email AS loser_email FROM claim_requests r JOIN items i ON r.item_id=i.id JOIN users u ON r.loser_id=u.id WHERE r.finder_id=? AND r.status='PENDING'"
                            : "SELECT r.request_id, r.item_id, r.finder_id, r.handover_place, r.status, r.is_appreciated, i.item_name, u.name AS finder_name FROM claim_requests r JOIN items i ON r.item_id=i.id JOIN users u ON r.finder_id=u.id WHERE r.loser_id=? ORDER BY r.request_id DESC";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, userId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            StringBuilder sb = new StringBuilder("[");
                            boolean first = true;
                            while (rs.next()) {
                                if (!first) sb.append(",");
                                first = false;
                                if (type.equals("finder")) {
                                    sb.append("{")
                                            .append("\"requestId\":").append(rs.getInt("request_id")).append(",")
                                            .append("\"loserId\":").append(rs.getInt("loser_id")).append(",")
                                            .append("\"itemName\":\"").append(escapeJson(rs.getString("item_name"))).append("\",")
                                            .append("\"loserName\":\"").append(escapeJson(rs.getString("loser_name"))).append("\",")
                                            .append("\"loserEmail\":\"").append(escapeJson(rs.getString("loser_email"))).append("\",")
                                            .append("\"secretMark\":\"").append(escapeJson(rs.getString("secret_mark"))).append("\"")
                                            .append("}");
                                } else {
                                    sb.append("{")
                                            .append("\"requestId\":").append(rs.getInt("request_id")).append(",")
                                            .append("\"finderId\":").append(rs.getInt("finder_id")).append(",")
                                            .append("\"itemName\":\"").append(escapeJson(rs.getString("item_name"))).append("\",")
                                            .append("\"finderName\":\"").append(escapeJson(rs.getString("finder_name"))).append("\",")
                                            .append("\"handoverPlace\":\"").append(escapeJson(rs.getString("handover_place"))).append("\",")
                                            .append("\"status\":\"").append(rs.getString("status")).append("\",")
                                            .append("\"isAppreciated\":").append(rs.getBoolean("is_appreciated"))
                                            .append("}");
                                }
                            }
                            sb.append("]");
                            sendResponse(exchange, 200, sb.toString());
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "[]"); }
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                int reqId = Integer.parseInt(map.get("requestId"));
                int loserId = Integer.parseInt(map.get("loserId"));
                String status = map.get("status");
                String place = map.getOrDefault("place", "");

                try (Connection conn = getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE claim_requests SET status=?, handover_place=? WHERE request_id=?")) {
                        stmt.setString(1, status);
                        stmt.setString(2, place);
                        stmt.setInt(3, reqId);
                        stmt.executeUpdate();
                    }

                    String msg = status.equals("ACCEPTED")
                            ? "🎉 Your claim was ACCEPTED! Meet finder at: " + place
                            : "❌ Secret mark did not match. Request was declined.";
                    try (PreparedStatement nStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
                        nStmt.setInt(1, loserId);
                        nStmt.setString(2, msg);
                        nStmt.executeUpdate();
                    }
                    sendResponse(exchange, 200, "{\"success\":true}");
                } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "{\"success\":false}"); }
            }
        }
    }

    static class StarAppreciateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> map = parseJson(readRequestBody(exchange));
                int reqId = Integer.parseInt(map.get("requestId"));
                int finderId = Integer.parseInt(map.get("finderId"));

                try (Connection conn = getConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET stars = COALESCE(stars, 0) + 1 WHERE id=?")) {
                        stmt.setInt(1, finderId);
                        stmt.executeUpdate();
                    }
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE claim_requests SET is_appreciated=TRUE WHERE request_id=?")) {
                        stmt.setInt(1, reqId);
                        stmt.executeUpdate();
                    }
    try (PreparedStatement nStmt = conn.prepareStatement("INSERT INTO notifications (user_id, message) VALUES (?, ?)")) {
    nStmt.setInt(1, finderId);
    nStmt.setString(2, "⭐ A grateful student awarded you an Honor Star for returning their item!");
    nStmt.executeUpdate();
}
                    sendResponse(exchange, 200, "{\"success\":true}");
                } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "{\"success\":false}"); }
            }
        }
    }

    static class NotificationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int userId = Integer.parseInt(exchange.getRequestURI().getQuery().split("userId=")[1].split("&")[0]);
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement("SELECT message FROM notifications WHERE user_id=? ORDER BY notif_id DESC LIMIT 5")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    StringBuilder sb = new StringBuilder("[");
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) sb.append(",");
                        first = false;
                        sb.append("{\"message\":\"").append(escapeJson(rs.getString("message"))).append("\"}");
                    }
                    sb.append("]");
                    sendResponse(exchange, 200, sb.toString());
                }
            } catch (Exception e) { e.printStackTrace(); sendResponse(exchange, 500, "[]"); }
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (kv.length == 2) {
                String key = kv[0].replaceAll("[\"{}]", "").trim();
                String val = kv[1].trim();
                if (val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
                map.put(key, val);
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}