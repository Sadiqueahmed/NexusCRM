package com.nexuscrm.backend.service;

import com.nexuscrm.backend.dto.DashboardStatsResponse;
import com.nexuscrm.backend.repository.LeadRepository;
import com.nexuscrm.backend.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for aggregating dashboard statistics.
 * Provides summary metrics for the React frontend's KPI cards and charts.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;

    public DashboardService(LeadRepository leadRepository,
                            TicketRepository ticketRepository) {
        this.leadRepository = leadRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Aggregates key CRM metrics for the dashboard overview.
     */
    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalLeads(leadRepository.count())
                .totalTickets(ticketRepository.count())
                .openTickets(ticketRepository.countByStatus("OPEN"))
                .resolvedTickets(ticketRepository.countByStatus("RESOLVED"))
                .aiHandledTickets(ticketRepository.countByAiHandled(true))
                .leadsByStatus(Map.of(
                        "NEW", leadRepository.countByStatus("NEW"),
                        "CONTACTED", leadRepository.countByStatus("CONTACTED"),
                        "QUALIFIED", leadRepository.countByStatus("QUALIFIED"),
                        "LOST", leadRepository.countByStatus("LOST"),
                        "CONVERTED", leadRepository.countByStatus("CONVERTED")
                ))
                .ticketsByPriority(Map.of(
                        "LOW", ticketRepository.countByStatus("LOW"),
                        "MEDIUM", ticketRepository.countByStatus("MEDIUM"),
                        "HIGH", ticketRepository.countByStatus("HIGH"),
                        "CRITICAL", ticketRepository.countByStatus("CRITICAL")
                ))
                .build();
    }
}
