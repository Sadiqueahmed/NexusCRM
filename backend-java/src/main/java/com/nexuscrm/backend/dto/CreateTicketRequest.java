package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO for creating a new support Ticket.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotBlank(message = "Subject is required")
    @Size(max = 500)
    private String subject;

    @NotBlank(message = "Description is required")
    private String description;

    private String priority;
    private String category;
    private String customerEmail;
    private Long assignedToId;
}
