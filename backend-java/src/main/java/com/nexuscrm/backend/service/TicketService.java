package com.nexuscrm.backend.service;

import com.nexuscrm.backend.dto.*;
import com.nexuscrm.backend.entity.Ticket;
import com.nexuscrm.backend.entity.User;
import com.nexuscrm.backend.event.CrmEvent;
import com.nexuscrm.backend.event.SseEventPublisher;
import com.nexuscrm.backend.exception.ResourceNotFoundException;
import com.nexuscrm.backend.repository.TicketRepository;
import com.nexuscrm.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for Ticket CRUD operations.
 * Publishes SSE events on ticket modifications, especially when
 * the AI agent resolves tickets autonomously.
 */
@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final SseEventPublisher sseEventPublisher;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         SseEventPublisher sseEventPublisher) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.sseEventPublisher = sseEventPublisher;
    }

    /**
     * Retrieves all tickets, optionally filtered by status and/or priority.
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(String status, String priority) {
        List<Ticket> tickets;

        if (status != null && !status.isBlank() && priority != null && !priority.isBlank()) {
            tickets = ticketRepository.findByStatusAndPriority(
                    status.toUpperCase(), priority.toUpperCase());
        } else if (status != null && !status.isBlank()) {
            tickets = ticketRepository.findByStatus(status.toUpperCase());
        } else if (priority != null && !priority.isBlank()) {
            tickets = ticketRepository.findByPriority(priority.toUpperCase());
        } else {
            tickets = ticketRepository.findAll();
        }

        return tickets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single ticket by ID.
     *
     * @throws ResourceNotFoundException if ticket not found
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
        return toResponse(ticket);
    }

    /**
     * Creates a new support ticket.
     */
    public TicketResponse createTicket(CreateTicketRequest request) {
        log.info("Creating new ticket: {}", request.getSubject());

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM")
                .category(request.getCategory())
                .customerEmail(request.getCustomerEmail())
                .build();

        if (request.getAssignedToId() != null) {
            User agent = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            ticket.setAssignedTo(agent);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created with ID: {}", saved.getId());

        // Broadcast creation event via SSE
        sseEventPublisher.publish(CrmEvent.builder()
                .type("TICKET_CREATED")
                .entity("TICKET")
                .entityId(saved.getId())
                .message(String.format("New ticket created: '%s' (Priority: %s)",
                        saved.getSubject(), saved.getPriority()))
                .details(Map.of("priority", saved.getPriority(), "category",
                        saved.getCategory() != null ? saved.getCategory() : "GENERAL"))
                .build());

        return toResponse(saved);
    }

    /**
     * Full update of an existing ticket.
     *
     * @throws ResourceNotFoundException if ticket not found
     */
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        if (request.getSubject() != null) ticket.setSubject(request.getSubject());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getStatus() != null) ticket.setStatus(request.getStatus().toUpperCase());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority().toUpperCase());
        if (request.getCategory() != null) ticket.setCategory(request.getCategory());
        if (request.getCustomerEmail() != null) ticket.setCustomerEmail(request.getCustomerEmail());
        if (request.getResolution() != null) ticket.setResolution(request.getResolution());
        if (request.getAiHandled() != null) ticket.setAiHandled(request.getAiHandled());

        if (request.getAssignedToId() != null) {
            User agent = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            ticket.setAssignedTo(agent);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket updated: ID={}", saved.getId());

        return toResponse(saved);
    }

    /**
     * Resolves a ticket with resolution notes.
     * This is the primary endpoint used by the AI agent's resolve_ticket tool.
     * Sets status to RESOLVED and optionally marks as AI-handled.
     *
     * @throws ResourceNotFoundException if ticket not found
     */
    public TicketResponse resolveTicket(Long id, ResolveTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));

        String oldStatus = ticket.getStatus();
        ticket.setStatus("RESOLVED");
        ticket.setResolution(request.getResolution());
        ticket.setAiHandled(request.getAiHandled() != null && request.getAiHandled());

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket resolved: ID={}, aiHandled={}", id, saved.getAiHandled());

        // Broadcast resolution event via SSE
        sseEventPublisher.publish(CrmEvent.builder()
                .type("TICKET_RESOLVED")
                .entity("TICKET")
                .entityId(saved.getId())
                .message(String.format("Ticket '%s' resolved%s",
                        saved.getSubject(),
                        saved.getAiHandled() ? " by AI Agent" : ""))
                .details(Map.of(
                        "oldStatus", oldStatus,
                        "resolution", saved.getResolution(),
                        "aiHandled", saved.getAiHandled()))
                .aiGenerated(saved.getAiHandled())
                .build());

        return toResponse(saved);
    }

    /**
     * Deletes a ticket by ID.
     *
     * @throws ResourceNotFoundException if ticket not found
     */
    public void deleteTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket", id);
        }
        ticketRepository.deleteById(id);
        log.info("Ticket deleted: ID={}", id);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Maps a Ticket entity to a TicketResponse DTO.
     */
    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .category(ticket.getCategory())
                .customerEmail(ticket.getCustomerEmail())
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToUsername(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null)
                .resolution(ticket.getResolution())
                .aiHandled(ticket.getAiHandled())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
