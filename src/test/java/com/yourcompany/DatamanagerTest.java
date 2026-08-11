package com.yourcompany;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

public class DatamanagerTest {
    private Datamanager datamanager;
    private String testUsername;
    private static final String TEST_PASSWORD = "TestPassword123";
    
    @Before
    public void setUp() {
        // Use unique username for each test to avoid conflicts
        testUsername = "testuser_" + UUID.randomUUID().toString().substring(0, 8);
        datamanager = new Datamanager();
    }
    
    @After
    public void tearDown() {
        // Clean up test database after each test
        try {
            Path dbPath = Paths.get(System.getProperty("user.home"), "clock-in-data", "clockin.db");
            if (Files.exists(dbPath)) {
                Files.delete(dbPath);
            }
            Path dbJournal = Paths.get(System.getProperty("user.home"), "clock-in-data", "clockin.db-journal");
            if (Files.exists(dbJournal)) {
                Files.delete(dbJournal);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    @Test
    public void testCreateUserSuccess() {
        String result = datamanager.createUser(testUsername, TEST_PASSWORD);
        assertNull("User creation should succeed", result);
    }
    
    @Test
    public void testCreateUserDuplicate() {
        datamanager.createUser(testUsername, TEST_PASSWORD);
        String result = datamanager.createUser(testUsername, "AnotherPassword123");
        assertNotNull("Duplicate user should fail", result);
        assertTrue("Should indicate duplicate", result.contains("already exists"));
    }
    
    @Test
    public void testCreateUserInvalidInput() {
        String result = datamanager.createUser("ab", "short");
        assertNotNull("Invalid input should fail", result);
        assertTrue("Should indicate validation error", result.contains("at least"));
    }
    
    @Test
    public void testVerifyUserSuccess() {
        datamanager.createUser(testUsername, TEST_PASSWORD);
        boolean result = datamanager.verifyUser(testUsername, TEST_PASSWORD);
        assertTrue("User verification should succeed", result);
    }
    
    @Test
    public void testVerifyUserFail() {
        datamanager.createUser(testUsername, TEST_PASSWORD);
        boolean result = datamanager.verifyUser(testUsername, "WrongPassword");
        assertFalse("User verification should fail with wrong password", result);
    }
    
    @Test
    public void testVerifyUserNonexistent() {
        boolean result = datamanager.verifyUser("nonexistent_user", TEST_PASSWORD);
        assertFalse("Nonexistent user should fail verification", result);
    }
    
    @Test
    public void testRecordClockIn() {
        datamanager.createUser(testUsername, TEST_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        String photoPath = "/tmp/photo.jpg";
        datamanager.recordClockIn(testUsername, now, photoPath);
        
        var records = datamanager.getClockRecords(testUsername, 1);
        assertEquals("Should have one clock record", 1, records.size());
        assertEquals("Username should match", testUsername, records.get(0)[1]);
        assertNotNull("Clock in time should be recorded", records.get(0)[2]);
    }
    
    @Test
    public void testRecordClockOut() {
        datamanager.createUser(testUsername, TEST_PASSWORD);
        LocalDateTime inTime = LocalDateTime.now().minusHours(1);
        LocalDateTime outTime = LocalDateTime.now();
        
        datamanager.recordClockIn(testUsername, inTime, "/tmp/in.jpg");
        datamanager.recordClockOut(testUsername, outTime, "/tmp/out.jpg");
        
        var records = datamanager.getClockRecords(testUsername, 1);
        assertEquals("Should have one clock record", 1, records.size());
        assertNotNull("Clock out time should be recorded", records.get(0)[3]);
    }
    
    @Test
    public void testGetAllClockRecords() {
        String user1 = "user1_" + UUID.randomUUID().toString().substring(0, 8);
        String user2 = "user2_" + UUID.randomUUID().toString().substring(0, 8);
        datamanager.createUser(user1, TEST_PASSWORD);
        datamanager.createUser(user2, TEST_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        
        datamanager.recordClockIn(user1, now, "/tmp/1.jpg");
        datamanager.recordClockIn(user2, now, "/tmp/2.jpg");
        
        var records = datamanager.getAllClockRecords();
        assertTrue("Should have at least 2 records", records.size() >= 2);
    }
}
