package com.nexuscrm.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for updating only the status of a Lead.
 * Used by the AI agent's update_lead_status tool.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateLeadStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    /** Optional reason for the status change (useful for AI audit trails) */
    private String reason;
}
