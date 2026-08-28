package com.nexuscrm.backend.dto;

import lombok.*;
import java.time.OffsetDateTime;

/**
 * Response DTO for Ticket data.
 * Includes the assigned agent's username for display purposes.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private String category;
    private String customerEmail;
    private Long assignedToId;
    private String assignedToUsername;
    private String resolution;
    private Boolean aiHandled;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
