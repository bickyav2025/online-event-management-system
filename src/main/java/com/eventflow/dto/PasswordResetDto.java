package com.eventflow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDto {

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    private String confirmPassword;

    public PasswordResetDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
