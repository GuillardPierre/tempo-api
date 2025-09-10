package com.tempo.application.model.passwordReset.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "Code invalide")
    private String code;

    @NotBlank
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String newPassword;
}
