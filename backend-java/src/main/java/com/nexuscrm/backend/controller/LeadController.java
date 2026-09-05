package com.nexuscrm.backend.controller;

import com.nexuscrm.backend.dto.*;
import com.nexuscrm.backend.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Lead management operations.
 * Provides full CRUD plus a dedicated status-update endpoint
 * used by the AI agent's update_lead_status tool.
 */
@RestController
@RequestMapping("/api/v1/leads")
@Tag(name = "Leads", description = "Sales Lead pipeline management endpoints")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    @Operation(summary = "List all leads",
            description = "Retrieves all leads, optionally filtered by pipeline status")
    public ResponseEntity<List<LeadResponse>> getAllLeads(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(leadService.getAllLeads(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lead by ID",
            description = "Retrieves a single lead by its ID")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long id) {
        return ResponseEntity.ok(leadService.getLeadById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new lead",
            description = "Creates a new lead in the sales pipeline")
    public ResponseEntity<LeadResponse> createLead(
            @Valid @RequestBody CreateLeadRequest request) {
        LeadResponse created = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update lead",
            description = "Full update of an existing lead's details")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadRequest request) {
        return ResponseEntity.ok(leadService.updateLead(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update lead status",
            description = "Updates only the pipeline status of a lead. " +
                    "Primary endpoint for the AI agent's update_lead_status tool.")
    public ResponseEntity<LeadResponse> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadStatusRequest request) {
        return ResponseEntity.ok(leadService.updateLeadStatus(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete lead",
            description = "Deletes a lead by its ID")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }
}
