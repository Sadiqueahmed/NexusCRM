package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for resolving a Ticket.
 * Used by the AI agent's resolve_ticket tool.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ResolveTicketRequest {

    @NotBlank(message = "Resolution notes are required")
    private String resolution;

    /** Whether this resolution was performed by the AI agent */
    @Builder.Default
    private Boolean aiHandled = false;
}
