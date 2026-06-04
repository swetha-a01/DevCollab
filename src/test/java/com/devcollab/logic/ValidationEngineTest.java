package com.devcollab.logic;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

public class ValidationEngineTest {

    @Test
    @DisplayName("Positive Test: Valid Student IDs should be accepted")
    public void testValidStudentIDs() {
        // Checking standard regular formats
        assertTrue(ValidationEngine.isValidStudentID("24BCA0001"));
        assertTrue(ValidationEngine.isValidStudentID("25BCA9999"));
        assertTrue(ValidationEngine.isValidStudentID("26BITS1234")); // 4-letter branch code check
        
        // Checking case insensitivity handling (should normalize to uppercase automatically)
        assertTrue(ValidationEngine.isValidStudentID("24bca0001")); 
    }

    @Test
    @DisplayName("Negative Test: Invalid Student IDs should be blocked")
    public void testInvalidStudentIDs() {
        // Too short or missing numbers
        assertFalse(ValidationEngine.isValidStudentID("24BCA")); 
        
        // Extra digits exceeding the length boundary profile
        assertFalse(ValidationEngine.isValidStudentID("24BCA00001")); 
        
        // Wrong pattern layout (numbers where letters should be)
        assertFalse(ValidationEngine.isValidStudentID("BCA240001")); 
        
        // Null or completely empty strings
        assertFalse(ValidationEngine.isValidStudentID("")); 
        assertFalse(ValidationEngine.isValidStudentID(null)); 
    }

    @Test
    @DisplayName("Positive Test: Authentic institutional emails should be accepted")
    public void testValidCampusEmails() {
        assertTrue(ValidationEngine.isValidCampusEmail("swetha.a2024@vitstudent.ac.in"));
        assertTrue(ValidationEngine.isValidCampusEmail("test.user@vitstudent.ac.in"));
        
        // Checking case insensitivity on the domain segment
        assertTrue(ValidationEngine.isValidCampusEmail("STUDENT@VITSTUDENT.AC.IN")); 
    }

    @Test
    @DisplayName("Negative Test: Non-campus email structures should be blocked")
    public void testInvalidCampusEmails() {
        // Standard personal providers must be rejected
        assertFalse(ValidationEngine.isValidCampusEmail("swetha@gmail.com"));
        assertFalse(ValidationEngine.isValidCampusEmail("dev@yahoo.com"));
        
        // Truncated variations or wrong domain endings
        assertFalse(ValidationEngine.isValidCampusEmail("student@vit.ac.in")); 
        assertFalse(ValidationEngine.isValidCampusEmail("student@vitstudent.com")); 
        
        // Null/Empty boundaries
        assertFalse(ValidationEngine.isValidCampusEmail(""));
        assertFalse(ValidationEngine.isValidCampusEmail(null));
    }
}