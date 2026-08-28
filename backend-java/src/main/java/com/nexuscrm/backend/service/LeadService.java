package com.nexuscrm.backend.service;

import com.nexuscrm.backend.dto.*;
import com.nexuscrm.backend.entity.Lead;
import com.nexuscrm.backend.entity.User;
import com.nexuscrm.backend.event.CrmEvent;
import com.nexuscrm.backend.event.SseEventPublisher;
import com.nexuscrm.backend.exception.ResourceNotFoundException;
import com.nexuscrm.backend.repository.LeadRepository;
import com.nexuscrm.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer for Lead CRUD operations.
 * Publishes SSE events when leads are modified, enabling
 * real-time dashboard updates and AI action visibility.
 */
@Service
@Transactional
public class LeadService {

    private static final Logger log = LoggerFactory.getLogger(LeadService.class);

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final SseEventPublisher sseEventPublisher;

    public LeadService(LeadRepository leadRepository,
                       UserRepository userRepository,
                       SseEventPublisher sseEventPublisher) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.sseEventPublisher = sseEventPublisher;
    }

    /**
     * Retrieves all leads, optionally filtered by pipeline status.
     */
    @Transactional(readOnly = true)
    public List<LeadResponse> getAllLeads(String status) {
        List<Lead> leads = (status != null && !status.isBlank())
                ? leadRepository.findByStatus(status.toUpperCase())
                : leadRepository.findAll();

        return leads.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single lead by ID.
     *
     * @throws ResourceNotFoundException if lead not found
     */
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));
        return toResponse(lead);
    }

    /**
     * Creates a new lead in the pipeline.
     */
    public LeadResponse createLead(CreateLeadRequest request) {
        log.info("Creating new lead: {} {}", request.getFirstName(), request.getLastName());

        Lead lead = Lead.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .company(request.getCompany())
                .source(request.getSource())
                .notes(request.getNotes())
                .build();

        // Resolve the assigned agent if provided
        if (request.getAssignedToId() != null) {
            User agent = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            lead.setAssignedTo(agent);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead created with ID: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Full update of an existing lead.
     *
     * @throws ResourceNotFoundException if lead not found
     */
    public LeadResponse updateLead(Long id, UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        if (request.getFirstName() != null) lead.setFirstName(request.getFirstName());
        if (request.getLastName() != null) lead.setLastName(request.getLastName());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getPhone() != null) lead.setPhone(request.getPhone());
        if (request.getCompany() != null) lead.setCompany(request.getCompany());
        if (request.getStatus() != null) lead.setStatus(request.getStatus());
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getNotes() != null) lead.setNotes(request.getNotes());
        if (request.getAiSummary() != null) lead.setAiSummary(request.getAiSummary());

        if (request.getAssignedToId() != null) {
            User agent = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssignedToId()));
            lead.setAssignedTo(agent);
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead updated: ID={}", saved.getId());

        // Broadcast update event via SSE
        sseEventPublisher.publish(CrmEvent.builder()
                .type("LEAD_UPDATED")
                .entity("LEAD")
                .entityId(saved.getId())
                .message(String.format("Lead '%s %s' updated",
                        saved.getFirstName(), saved.getLastName()))
                .details(Map.of("status", saved.getStatus()))
                .build());

        return toResponse(saved);
    }

    /**
     * Updates only the pipeline status of a lead.
     * This is the primary endpoint used by the AI agent's update_lead_status tool.
     *
     * @throws ResourceNotFoundException if lead not found
     */
    public LeadResponse updateLeadStatus(Long id, UpdateLeadStatusRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id));

        String oldStatus = lead.getStatus();
        lead.setStatus(request.getStatus().toUpperCase());

        // Append the reason to notes if provided
        if (request.getReason() != null && !request.getReason().isBlank()) {
            String existingNotes = lead.getNotes() != null ? lead.getNotes() : "";
            lead.setNotes(existingNotes + "\n[AI] Status changed: " + request.getReason());
        }

        Lead saved = leadRepository.save(lead);
        log.info("Lead status updated: ID={}, {} → {}", id, oldStatus, saved.getStatus());

        // Broadcast status change event via SSE (AI action)
        sseEventPublisher.publish(CrmEvent.builder()
                .type("LEAD_UPDATED")
                .entity("LEAD")
                .entityId(saved.getId())
                .message(String.format("Lead '%s %s' status changed: %s → %s",
                        saved.getFirstName(), saved.getLastName(),
                        oldStatus, saved.getStatus()))
                .details(Map.of(
                        "oldStatus", oldStatus,
                        "newStatus", saved.getStatus(),
                        "reason", request.getReason() != null ? request.getReason() : ""))
                .aiGenerated(true)
                .build());

        return toResponse(saved);
    }

    /**
     * Deletes a lead by ID.
     *
     * @throws ResourceNotFoundException if lead not found
     */
    public void deleteLead(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Lead", id);
        }
        leadRepository.deleteById(id);
        log.info("Lead deleted: ID={}", id);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Maps a Lead entity to a LeadResponse DTO.
     */
    private LeadResponse toResponse(Lead lead) {
        return LeadResponse.builder()
                .id(lead.getId())
                .firstName(lead.getFirstName())
                .lastName(lead.getLastName())
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .company(lead.getCompany())
                .status(lead.getStatus())
                .source(lead.getSource())
                .assignedToId(lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null)
                .assignedToUsername(lead.getAssignedTo() != null ? lead.getAssignedTo().getUsername() : null)
                .notes(lead.getNotes())
                .aiSummary(lead.getAiSummary())
                .createdAt(lead.getCreatedAt())
                .updatedAt(lead.getUpdatedAt())
                .build();
    }
}
