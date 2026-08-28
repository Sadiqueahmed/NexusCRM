package com.nexuscrm.backend.dto;

import lombok.*;
import java.time.OffsetDateTime;

/**
 * Response DTO for Lead data.
 * Includes the assigned agent's username for display purposes.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LeadResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String company;
    private String status;
    private String source;
    private Long assignedToId;
    private String assignedToUsername;
    private String notes;
    private String aiSummary;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
