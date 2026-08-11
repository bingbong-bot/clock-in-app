package com.yourcompany;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class DatamanagerTest {
    private Datamanager datamanager;
    private static final String TEST_USER = "testuser";
    private static final String TEST_PASSWORD = "TestPassword123";
    
    @Before
    public void setUp() {
        datamanager = new Datamanager();
    }
    
    @Test
    public void testCreateUserSuccess() {
        String result = datamanager.createUser(TEST_USER, TEST_PASSWORD);
        assertNull("User creation should succeed", result);
    }
    
    @Test
    public void testCreateUserDuplicate() {
        datamanager.createUser(TEST_USER, TEST_PASSWORD);
        String result = datamanager.createUser(TEST_USER, "AnotherPassword123");
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
        datamanager.createUser(TEST_USER, TEST_PASSWORD);
        boolean result = datamanager.verifyUser(TEST_USER, TEST_PASSWORD);
        assertTrue("User verification should succeed", result);
    }
    
    @Test
    public void testVerifyUserFail() {
        datamanager.createUser(TEST_USER, TEST_PASSWORD);
        boolean result = datamanager.verifyUser(TEST_USER, "WrongPassword");
        assertFalse("User verification should fail with wrong password", result);
    }
    
    @Test
    public void testVerifyUserNonexistent() {
        boolean result = datamanager.verifyUser("nonexistent", TEST_PASSWORD);
        assertFalse("Nonexistent user should fail verification", result);
    }
    
    @Test
    public void testRecordClockIn() {
        datamanager.createUser(TEST_USER, TEST_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        String photoPath = "/tmp/photo.jpg";
        datamanager.recordClockIn(TEST_USER, now, photoPath);
        
        var records = datamanager.getClockRecords(TEST_USER, 1);
        assertEquals("Should have one clock record", 1, records.size());
        assertEquals("Username should match", TEST_USER, records.get(0)[1]);
        assertNotNull("Clock in time should be recorded", records.get(0)[2]);
    }
    
    @Test
    public void testRecordClockOut() {
        datamanager.createUser(TEST_USER, TEST_PASSWORD);
        LocalDateTime inTime = LocalDateTime.now().minusHours(1);
        LocalDateTime outTime = LocalDateTime.now();
        
        datamanager.recordClockIn(TEST_USER, inTime, "/tmp/in.jpg");
        datamanager.recordClockOut(TEST_USER, outTime, "/tmp/out.jpg");
        
        var records = datamanager.getClockRecords(TEST_USER, 1);
        assertEquals("Should have one clock record", 1, records.size());
        assertNotNull("Clock out time should be recorded", records.get(0)[3]);
    }
    
    @Test
    public void testGetAllClockRecords() {
        datamanager.createUser("user1", TEST_PASSWORD);
        datamanager.createUser("user2", TEST_PASSWORD);
        LocalDateTime now = LocalDateTime.now();
        
        datamanager.recordClockIn("user1", now, "/tmp/1.jpg");
        datamanager.recordClockIn("user2", now, "/tmp/2.jpg");
        
        var records = datamanager.getAllClockRecords();
        assertTrue("Should have at least 2 records", records.size() >= 2);
    }
}
