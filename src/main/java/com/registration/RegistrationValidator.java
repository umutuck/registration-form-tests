package com.registration;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validates all fields of the Create New Account registration form.
 *
 * Rules
 * ─────
 * First Name  : 2–50 chars, letters/spaces/hyphens only
 * Last Name   : 2–50 chars, letters/spaces/hyphens only
 * E-mail      : standard email format, max 100 chars
 * Date of Birth: dd/mm/yyyy, user must be between 13 and 120 years old
 * Password    : 8–64 chars, at least one uppercase, one lowercase,
 *               one digit, one special character
 * Confirm Pwd : must match Password exactly
 */
public class RegistrationValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-zÇçĞğİıÖöŞşÜü][A-Za-zÇçĞğİıÖöŞşÜü \\-]{1,49}$");

    private static final DateTimeFormatter DOB_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ------------------------------------------------------------------ //
    //  Public API
    // ------------------------------------------------------------------ //

    public ValidationResult validate(RegistrationForm form) {
        ValidationResult result = new ValidationResult();

        validateFirstName(form.getFirstName(),  result);
        validateLastName (form.getLastName(),   result);
        validateEmail    (form.getEmail(),      result);
        validateDob      (form.getDateOfBirth(),result);
        validatePassword (form.getPassword(),   result);
        validateConfirm  (form.getPassword(), form.getConfirmPassword(), result);

        return result;
    }

    // ------------------------------------------------------------------ //
    //  Field validators
    // ------------------------------------------------------------------ //

    private void validateFirstName(String value, ValidationResult r) {
        if (isNullOrBlank(value)) {
            r.addError("First name is required.");
            return;
        }
        if (value.length() < 2) {
            r.addError("First name must be at least 2 characters.");
            return;
        }
        if (value.length() > 50) {
            r.addError("First name must be at most 50 characters.");
            return;
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            r.addError("First name contains invalid characters.");
        }
    }

    private void validateLastName(String value, ValidationResult r) {
        if (isNullOrBlank(value)) {
            r.addError("Last name is required.");
            return;
        }
        if (value.length() < 2) {
            r.addError("Last name must be at least 2 characters.");
            return;
        }
        if (value.length() > 50) {
            r.addError("Last name must be at most 50 characters.");
            return;
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            r.addError("Last name contains invalid characters.");
        }
    }

    private void validateEmail(String value, ValidationResult r) {
        if (isNullOrBlank(value)) {
            r.addError("Email is required.");
            return;
        }
        if (value.length() > 100) {
            r.addError("Email must be at most 100 characters.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            r.addError("Email format is invalid.");
        }
    }

    private void validateDob(String value, ValidationResult r) {
        if (isNullOrBlank(value)) {
            r.addError("Date of birth is required.");
            return;
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(value, DOB_FORMATTER);
        } catch (DateTimeParseException e) {
            r.addError("Date of birth must be in dd/mm/yyyy format.");
            return;
        }
        int age = Period.between(dob, LocalDate.now()).getYears();
        if (age < 13) {
            r.addError("User must be at least 13 years old.");
        } else if (age > 120) {
            r.addError("Date of birth is not realistic.");
        }
    }

    private void validatePassword(String value, ValidationResult r) {
        if (isNullOrBlank(value)) {
            r.addError("Password is required.");
            return;
        }
        if (value.length() < 8) {
            r.addError("Password must be at least 8 characters.");
            return;
        }
        if (value.length() > 64) {
            r.addError("Password must be at most 64 characters.");
            return;
        }
        if (!value.matches(".*[A-Z].*")) {
            r.addError("Password must contain at least one uppercase letter.");
        }
        if (!value.matches(".*[a-z].*")) {
            r.addError("Password must contain at least one lowercase letter.");
        }
        if (!value.matches(".*[0-9].*")) {
            r.addError("Password must contain at least one digit.");
        }
        if (!value.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            r.addError("Password must contain at least one special character.");
        }
    }

    private void validateConfirm(String password, String confirm, ValidationResult r) {
        if (isNullOrBlank(confirm)) {
            r.addError("Confirm password is required.");
            return;
        }
        if (!confirm.equals(password)) {
            r.addError("Passwords do not match.");
        }
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
