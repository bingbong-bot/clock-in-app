package com.yourcompany;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Datamanager {
    private static final Logger logger = LoggerFactory.getLogger(Datamanager.class);
    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), "clock-in-data");
    private static final Path DB_PATH = DATA_DIR.resolve("clockin.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH.toString();
    private static final int BCRYPT_STRENGTH = 12;
    
    public Datamanager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Files.createDirectories(DATA_DIR);
        } catch (Exception e) {
            logger.error("Error creating data directory", e);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            String sql = "CREATE TABLE IF NOT EXISTS clock_records (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "username TEXT NOT NULL, " +
                         "clock_in_time TEXT, " +
                         "clock_out_time TEXT, " +
                         "clock_in_photo TEXT, " +
                         "clock_out_photo TEXT)";
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            logger.error("Error creating clock_records table", e);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS users (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "username TEXT NOT NULL UNIQUE, " +
                         "password_hash TEXT NOT NULL, " +
                         "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                         "last_login TEXT)";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            logger.error("Error creating users table", e);
        }
    }

    public void recordClockIn(String username, LocalDateTime time, String photoPath) {
        String sql = "INSERT INTO clock_records (username, clock_in_time, clock_in_photo) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, time.toString());
            pstmt.setString(3, photoPath);
            pstmt.executeUpdate();
            logger.info("Clock-in recorded for user: {}", username);
        } catch (SQLException e) {
            logger.error("Error saving clock-in for user: {}", username, e);
        }
    }

    public void recordClockOut(String username, LocalDateTime time, String photoPath) {
        String findSql = "SELECT id FROM clock_records WHERE username = ? AND clock_out_time IS NULL ORDER BY id DESC LIMIT 1";
        String updateSql = "UPDATE clock_records SET clock_out_time = ?, clock_out_photo = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement findStmt = conn.prepareStatement(findSql)) {
            findStmt.setString(1, username);
            ResultSet rs = findStmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, time.toString());
                    updateStmt.setString(2, photoPath);
                    updateStmt.setInt(3, id);
                    updateStmt.executeUpdate();
                    logger.info("Clock-out recorded for user: {}", username);
                }
            } else {
                String insertSql = "INSERT INTO clock_records (username, clock_out_time, clock_out_photo) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, username);
                    insertStmt.setString(2, time.toString());
                    insertStmt.setString(3, photoPath);
                    insertStmt.executeUpdate();
                    logger.warn("Clock-out recorded without clock-in for user: {}", username);
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving clock-out for user: {}", username, e);
        }
    }

    public Path getDataDir() {
        return DATA_DIR;
    }

    public List<String[]> getAllClockRecords() {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT id, username, clock_in_time, clock_out_time, clock_in_photo, clock_out_photo " +
                     "FROM clock_records ORDER BY id DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[] {
                    String.valueOf(rs.getInt("id")),
                    rs.getString("username"),
                    rs.getString("clock_in_time"),
                    rs.getString("clock_out_time"),
                    rs.getString("clock_in_photo"),
                    rs.getString("clock_out_photo")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error reading clock records: " + e.getMessage());
        }
        return rows;
    }

    public List<String[]> getClockRecords(String usernameFilter, Integer limit) {
        List<String[]> rows = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, username, clock_in_time, clock_out_time, clock_in_photo, clock_out_photo " +
            "FROM clock_records"
        );
        List<Object> params = new ArrayList<>();
        if (usernameFilter != null && !usernameFilter.isBlank()) {
            sql.append(" WHERE username = ?");
            params.add(usernameFilter);
        }
        sql.append(" ORDER BY id DESC");
        if (limit != null && limit > 0) {
            sql.append(" LIMIT ?");
            params.add(limit);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[] {
                        String.valueOf(rs.getInt("id")),
                        rs.getString("username"),
                        rs.getString("clock_in_time"),
                        rs.getString("clock_out_time"),
                        rs.getString("clock_in_photo"),
                        rs.getString("clock_out_photo")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("Error reading clock records: " + e.getMessage());
        }
        return rows;
    }

    public List<String> getUsernames() {
        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("Error reading users: " + e.getMessage());
        }
        return users;
    }

    public String createUser(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return "Username and password are required.";
        }
        if (username.length() < 3 || password.length() < 6) {
            return "Username must be at least 3 characters and password at least 6 characters.";
        }
        
        String hashedPassword = BCrypt.withDefaults().hashToString(BCRYPT_STRENGTH, password.toCharArray());
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            logger.info("User created: {}", username);
            return null;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                return "Username already exists.";
            }
            logger.error("Error creating account for user: {}", username, e);
            return "Error creating account: " + e.getMessage();
        }
    }

    public boolean verifyUser(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            return false;
        }
        String sql = "SELECT password_hash FROM users WHERE username = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    boolean verified = BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified;
                    if (verified) {
                        updateLastLogin(username);
                        logger.info("User verified: {}", username);
                    }
                    return verified;
                }
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error verifying user: {}", username, e);
            return false;
        }
    }
    
    private void updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating last login for user: {}", username, e);
        }
    }

    public QueryResult query(String sql) {
        QueryResult result = new QueryResult();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                result.columns.add(meta.getColumnLabel(i));
            }
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= colCount; i++) {
                    row.add(rs.getString(i));
                }
                result.rows.add(row);
            }
        } catch (SQLException e) {
            result.error = "Query failed: " + e.getMessage();
        }
        return result;
    }

    public static class QueryResult {
        public final List<String> columns = new ArrayList<>();
        public final List<List<String>> rows = new ArrayList<>();
        public String error;
    }
}
