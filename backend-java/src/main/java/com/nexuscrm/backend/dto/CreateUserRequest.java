package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a new CRM user.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be 3-100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be at least 8 characters")
    private String password;

    private String role;
}
