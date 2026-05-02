package com.registration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RegistrationValidator}.
 *
 * Techniques used
 * ───────────────
 *  • Equivalence Partitioning (EP) – valid/invalid partitions for each field
 *  • Boundary Value Analysis  (BVA) – min/max lengths, age limits (13 / 120)
 *  • Parameterized tests           – multiple values in one test method
 *  • Setup & Teardown              – @BeforeEach / @AfterEach
 */
@DisplayName("Registration Form – Validator Tests")
class RegistrationValidatorTest {

    private RegistrationValidator validator;   // shared under test
    private long startTime;                    // used in teardown logging

    // ================================================================== //
    //  Setup & Teardown
    // ================================================================== //

    /**
     * Runs before EVERY test method.
     * Creates a fresh validator and records start time.
     */
    @BeforeEach
    void setUp() {
        validator = new RegistrationValidator();
        startTime = System.currentTimeMillis();
        System.out.println("▶ Starting: " + Thread.currentThread().getStackTrace()[2].getMethodName());
    }

    /**
     * Runs after EVERY test method.
     * Prints elapsed time and nullifies the validator.
     */
    @AfterEach
    void tearDown() {
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("✔ Finished in " + elapsed + " ms\n");
        validator = null;
    }

    // ================================================================== //
    //  Helper – builds a fully valid form so individual fields can be
    //  overridden without repeating boilerplate everywhere.
    // ================================================================== //

    private RegistrationForm validForm() {
        return new RegistrationForm(
                "Alice",
                "Smith",
                "alice.smith@example.com",
                "15/06/1995",        // age ~29
                "Secret@123",
                "Secret@123"
        );
    }

    // ================================================================== //
    //  TC-01  Happy Path – all valid inputs                               //
    // ================================================================== //

    @Test
    @DisplayName("TC-01 | EP | All valid inputs → form is accepted")
    void tc01_allValidInputs_shouldPass() {
        ValidationResult result = validator.validate(validForm());
        assertTrue(result.isValid(), "Expected no errors but got: " + result.getErrors());
    }

    // ================================================================== //
    //  FIRST NAME tests (TC-02 – TC-06)                                  //
    // ================================================================== //

    @Test
    @DisplayName("TC-02 | EP | First name is null → error")
    void tc02_firstNameNull_shouldFail() {
        RegistrationForm form = new RegistrationForm(null, "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("first name is required"), result.getErrors().toString());
    }

    @Test
    @DisplayName("TC-03 | BVA | First name = 1 char (below min boundary) → error")
    void tc03_firstNameOneChar_shouldFail() {
        RegistrationForm form = new RegistrationForm("A", "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("first name must be at least"));
    }

    @Test
    @DisplayName("TC-04 | BVA | First name = 2 chars (min boundary) → valid")
    void tc04_firstNameTwoChars_shouldPass() {
        RegistrationForm form = new RegistrationForm("Al", "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        assertTrue(validator.validate(form).isValid());
    }

    @Test
    @DisplayName("TC-05 | BVA | First name = 51 chars (above max boundary) → error")
    void tc05_firstNameFiftyOneChars_shouldFail() {
        String longName = "A".repeat(51);
        RegistrationForm form = new RegistrationForm(longName, "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("first name must be at most"));
    }

    @Test
    @DisplayName("TC-06 | EP | First name with digits → error")
    void tc06_firstNameWithDigits_shouldFail() {
        RegistrationForm form = new RegistrationForm("Al1ce", "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("invalid characters"));
    }

    // ================================================================== //
    //  EMAIL tests (TC-07 – TC-10)                                       //
    // ================================================================== //

    @ParameterizedTest(name = "TC-07 | EP | Invalid email: {0}")
    @DisplayName("TC-07 | EP | Various invalid email formats")
    @ValueSource(strings = {
            "notanemail",
            "missing@",
            "@nodomain.com",
            "spaces in@email.com",
            "double@@at.com"
    })
    void tc07_invalidEmailFormats_shouldFail(String email) {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", email, "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("email format is invalid") || result.hasError("email is required"));
    }

    @Test
    @DisplayName("TC-08 | EP | Valid email with subdomain → accepted")
    void tc08_validEmailWithSubdomain_shouldPass() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "alice@mail.example.co.uk", "15/06/1995", "Secret@123", "Secret@123");
        assertTrue(validator.validate(form).isValid());
    }

    @Test
    @DisplayName("TC-09 | BVA | Email exactly 100 chars (max boundary) → accepted")
    void tc09_emailExactlyHundredChars_shouldPass() {
        // local@domain.tld padded to exactly 100 chars
        String local = "a".repeat(62);        // 62 + 1(@) + 30(domain) + 1(.) + 3(tld) = 97? let's be exact
        // build: local(n) + "@test.com" must be 100 chars total → local = 91 chars
        String localPart = "a".repeat(91);
        String email = localPart + "@t.co"; // 91+1+1+1+2 = 96... easier to just measure
        email = "a".repeat(88) + "@test.com"; // 88+9 = 97, pad to 100
        email = "a".repeat(91) + "@b.co";    // 91+5=96
        email = "a".repeat(93) + "@b.co";    // 93+5=98
        email = "a".repeat(95) + "@b.co";    // 95+5=100  ✓
        assertEquals(100, email.length());
        RegistrationForm form = new RegistrationForm("Alice", "Smith", email, "15/06/1995", "Secret@123", "Secret@123");
        assertTrue(validator.validate(form).isValid());
    }

    @Test
    @DisplayName("TC-10 | BVA | Email 101 chars (above max) → error")
    void tc10_emailHundredOneChars_shouldFail() {
        String email = "a".repeat(96) + "@b.co"; // 96+5=101
        assertEquals(101, email.length());
        RegistrationForm form = new RegistrationForm("Alice", "Smith", email, "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("email must be at most"));
    }

    // ================================================================== //
    //  DATE OF BIRTH tests (TC-11 – TC-14)                               //
    // ================================================================== //

    @Test
    @DisplayName("TC-11 | EP | DOB wrong format (yyyy-mm-dd) → error")
    void tc11_dobWrongFormat_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "1995-06-15", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("dd/mm/yyyy"));
    }

    @Test
    @DisplayName("TC-12 | BVA | Age exactly 13 (min boundary) → accepted")
    void tc12_ageExactlyThirteen_shouldPass() {
        // Calculate a date that is exactly 13 years ago today
        java.time.LocalDate thirteenYearsAgo = java.time.LocalDate.now().minusYears(13);
        String dob = String.format("%02d/%02d/%04d",
                thirteenYearsAgo.getDayOfMonth(),
                thirteenYearsAgo.getMonthValue(),
                thirteenYearsAgo.getYear());
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", dob, "Secret@123", "Secret@123");
        assertTrue(validator.validate(form).isValid(), "Age exactly 13 should be accepted");
    }

    @Test
    @DisplayName("TC-13 | BVA | Age 12 (below min boundary) → error")
    void tc13_ageTwelve_shouldFail() {
        java.time.LocalDate twelveYearsAgo = java.time.LocalDate.now().minusYears(12).plusDays(1);
        String dob = String.format("%02d/%02d/%04d",
                twelveYearsAgo.getDayOfMonth(),
                twelveYearsAgo.getMonthValue(),
                twelveYearsAgo.getYear());
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", dob, "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("at least 13"));
    }

    @Test
    @DisplayName("TC-14 | BVA | Age > 120 (unrealistic) → error")
    void tc14_ageOverHundredTwenty_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "01/01/1890", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("not realistic"));
    }

    // ================================================================== //
    //  PASSWORD tests (TC-15 – TC-19)                                    //
    // ================================================================== //

    @Test
    @DisplayName("TC-15 | BVA | Password = 7 chars (below min) → error")
    void tc15_passwordSevenChars_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "Se@1234", "Se@1234");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("at least 8 characters"));
    }

    @Test
    @DisplayName("TC-16 | BVA | Password = 8 chars (min boundary) → accepted")
    void tc16_passwordEightChars_shouldPass() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "Secret@1", "Secret@1");
        assertTrue(validator.validate(form).isValid());
    }

    @Test
    @DisplayName("TC-17 | EP | Password missing special character → error")
    void tc17_passwordNoSpecialChar_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "Secret123", "Secret123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("special character"));
    }

    @Test
    @DisplayName("TC-18 | EP | Password all lowercase → missing uppercase error")
    void tc18_passwordAllLowercase_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "secret@123", "secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("uppercase"));
    }

    @ParameterizedTest(name = "TC-19 | EP | Password variation: {0}")
    @DisplayName("TC-19 | EP | Various weak passwords")
    @CsvSource({
            "password,   missing upper+digit+special",
            "PASSWORD1@, missing lowercase",
            "Pass@word,  missing digit",
            "P@ss1,      too short"
    })
    void tc19_weakPasswords_shouldFail(String password, String reason) {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", password.trim(), password.trim());
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid(), "Should fail because: " + reason);
    }

    // ================================================================== //
    //  CONFIRM PASSWORD tests (TC-20 – TC-21)                            //
    // ================================================================== //

    @Test
    @DisplayName("TC-20 | EP | Passwords don't match → error")
    void tc20_passwordMismatch_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@456");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("do not match"));
    }

    @Test
    @DisplayName("TC-21 | EP | Confirm password empty → error")
    void tc21_confirmPasswordEmpty_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", "Secret@123", "");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("confirm password is required"));
    }

    // ================================================================== //
    //  MULTI-FIELD / EDGE CASE tests (TC-22 – TC-25)                     //
    // ================================================================== //

    @Test
    @DisplayName("TC-22 | EP | All fields blank → multiple errors")
    void tc22_allFieldsBlank_shouldHaveManyErrors() {
        RegistrationForm form = new RegistrationForm("", "", "", "", "", "");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 6, "Expected at least 6 errors, got: " + result.getErrors());
    }

    @Test
    @DisplayName("TC-23 | EP | SQL injection in name → rejected as invalid chars")
    void tc23_sqlInjectionInName_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice'; DROP TABLE users;--", "Smith", "a@b.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
        assertTrue(result.hasError("invalid characters"));
    }

    @Test
    @DisplayName("TC-24 | EP | XSS script tag in email → rejected")
    void tc24_xssInEmail_shouldFail() {
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "<script>alert(1)</script>@evil.com", "15/06/1995", "Secret@123", "Secret@123");
        ValidationResult result = validator.validate(form);
        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("TC-25 | BVA | Password exactly 64 chars (max boundary) → accepted")
    void tc25_passwordMaxBoundary_shouldPass() {
        // 64-char password with all required char types
        String pwd = "Aa1!" + "x".repeat(60); // 4 + 60 = 64
        assertEquals(64, pwd.length());
        RegistrationForm form = new RegistrationForm("Alice", "Smith", "a@b.com", "15/06/1995", pwd, pwd);
        assertTrue(validator.validate(form).isValid());
    }
}
