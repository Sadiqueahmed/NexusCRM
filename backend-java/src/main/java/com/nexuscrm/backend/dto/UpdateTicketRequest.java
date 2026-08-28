package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for updating an existing Ticket.
 * All fields are optional — only provided fields are updated.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateTicketRequest {

    @Size(max = 500)
    private String subject;

    private String description;
    private String status;
    private String priority;
    private String category;
    private String customerEmail;
    private Long assignedToId;
    private String resolution;
    private Boolean aiHandled;
}
