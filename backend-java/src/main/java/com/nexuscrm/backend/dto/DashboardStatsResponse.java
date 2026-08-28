package com.nexuscrm.backend.dto;

import lombok.*;
import java.util.Map;

/**
 * Response DTO for dashboard overview statistics.
 * Aggregates key metrics across leads, tickets, and AI activity.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalLeads;
    private long totalTickets;
    private long openTickets;
    private long resolvedTickets;
    private long aiHandledTickets;
    private Map<String, Long> leadsByStatus;
    private Map<String, Long> ticketsByPriority;
}
