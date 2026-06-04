package com.devcollab.logic;

import java.util.regex.Pattern;

public class ValidationEngine {

    // Regex pattern: 2 digits + 3 or 4 uppercase letters + 4 digits (e.g., 24BCA0123)
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{2}[A-Z]{3,4}\\d{4}$");
    
    // Explicit campus institutional email constraint
    private static final String CAMPUS_DOMAIN = "@vitstudent.ac.in";

    public static boolean isValidStudentID(String studentId) {
        if (studentId == null) return false;
        return ID_PATTERN.matcher(studentId.toUpperCase().trim()).matches();
    }

    public static boolean isValidCampusEmail(String email) {
        if (email == null) return false;
        return email.toLowerCase().trim().endsWith(CAMPUS_DOMAIN);
    }
}