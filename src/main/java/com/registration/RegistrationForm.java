package com.registration;

/**
 * Represents the Create New Account form data model.
 * Holds all field values submitted by the user.
 */
public class RegistrationForm {

    private String firstName;
    private String lastName;
    private String email;
    private String dateOfBirth;   // format: dd/mm/yyyy
    private String password;
    private String confirmPassword;

    public RegistrationForm(String firstName, String lastName, String email,
                            String dateOfBirth, String password, String confirmPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters
    public String getFirstName()       { return firstName; }
    public String getLastName()        { return lastName; }
    public String getEmail()           { return email; }
    public String getDateOfBirth()     { return dateOfBirth; }
    public String getPassword()        { return password; }
    public String getConfirmPassword() { return confirmPassword; }
}
