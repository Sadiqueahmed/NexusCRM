package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a new Lead.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateLeadRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Email(message = "Must be a valid email address")
    private String email;

    private String phone;
    private String company;
    private String source;
    private Long assignedToId;
    private String notes;
}
