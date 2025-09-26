package com.example.hotel.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Input validation utilities
 */
public class ValidationUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[1-9]\\d{1,14}$|^\\d{10}$|^\\(\\d{3}\\)\\s?\\d{3}-?\\d{4}$"
    );
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Validate if string is not null and not blank
     */
    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate if string has minimum length
     */
    public static boolean hasMinLength(String value, int minLength) {
        return isValidString(value) && value.trim().length() >= minLength;
    }
    
    /**
     * Validate if string has maximum length
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return isValidString(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return isValidString(phone) && PHONE_PATTERN.matcher(phone.replaceAll("\\s", "")).matches();
    }
    
    /**
     * Validate if integer is positive
     */
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }
    
    /**
     * Validate if integer is within range
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * Validate date string and parse it
     */
    public static LocalDate parseDate(String dateString) throws DateTimeParseException {
        if (!isValidString(dateString)) {
            throw new DateTimeParseException("Date string cannot be null or empty", dateString, 0);
        }
        return LocalDate.parse(dateString.trim(), DATE_FORMATTER);
    }
    
    /**
     * Validate if date is not in the past
     */
    public static boolean isNotPastDate(LocalDate date) {
        return date != null && !date.isBefore(LocalDate.now());
    }
    
    /**
     * Validate if start date is before end date
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate != null && endDate != null && startDate.isBefore(endDate);
    }
    
    /**
     * Validate room number format (alphanumeric)
     */
    public static boolean isValidRoomNumber(String roomNumber) {
        return isValidString(roomNumber) && roomNumber.trim().matches("^[A-Za-z0-9]+$");
    }
    
    /**
     * Validate ID format (alphanumeric with possible hyphens)
     */
    public static boolean isValidId(String id) {
        return isValidString(id) && id.trim().matches("^[A-Za-z0-9-]+$");
    }
    
    /**
     * Sanitize input string (trim and remove special characters for safety)
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"'&]", "");
    }
    
    /**
     * Validate capacity range
     */
    public static boolean isValidCapacity(int capacity) {
        return isInRange(capacity, 1, 10); // Reasonable hotel room capacity
    }
    
    /**
     * Validate party size
     */
    public static boolean isValidPartySize(int partySize) {
        return isInRange(partySize, 1, 20); // Reasonable party size
    }
    
    /**
     * Comprehensive validation result
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
    }
    
    /**
     * Validate guest information
     */
    public static ValidationResult validateGuest(String firstName, String lastName, String email, String phone) {
        if (!isValidString(firstName)) {
            return ValidationResult.invalid("First name is required");
        }
        if (!hasMinLength(firstName, 2)) {
            return ValidationResult.invalid("First name must be at least 2 characters");
        }
        if (!isValidString(lastName)) {
            return ValidationResult.invalid("Last name is required");
        }
        if (!hasMinLength(lastName, 2)) {
            return ValidationResult.invalid("Last name must be at least 2 characters");
        }
        if (!isValidEmail(email)) {
            return ValidationResult.invalid("Valid email address is required");
        }
        if (!isValidPhone(phone)) {
            return ValidationResult.invalid("Valid phone number is required");
        }
        return ValidationResult.valid();
    }
}