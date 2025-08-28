package com.tempo.application.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDto {
    @NotBlank(message = "username is mandatory")
    private String username;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email format is invalid")
    private String email;
    @NotBlank(message = "Password is mandatory")
    @Size(min = 4, message = "Password must be at least 8 characters long")
    private String password;
}
