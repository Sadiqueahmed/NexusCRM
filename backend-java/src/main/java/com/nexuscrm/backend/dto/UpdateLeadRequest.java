package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for updating an existing Lead.
 * All fields are optional — only provided fields are updated.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateLeadRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email(message = "Must be a valid email address")
    private String email;

    private String phone;
    private String company;
    private String status;
    private String source;
    private Long assignedToId;
    private String notes;
    private String aiSummary;
}
