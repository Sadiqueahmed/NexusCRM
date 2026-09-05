package com.nexuscrm.backend.controller;

import com.nexuscrm.backend.dto.*;
import com.nexuscrm.backend.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Ticket management operations.
 * Provides full CRUD plus a dedicated resolve endpoint
 * used by the AI agent's resolve_ticket tool.
 */
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets", description = "Customer support ticket management endpoints")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(summary = "List all tickets",
            description = "Retrieves all tickets, optionally filtered by status and/or priority")
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        return ResponseEntity.ok(ticketService.getAllTickets(status, priority));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID",
            description = "Retrieves a single ticket by its ID")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new ticket",
            description = "Creates a new customer support ticket")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        TicketResponse created = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket",
            description = "Full update of an existing ticket's details")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Resolve ticket",
            description = "Resolves a ticket with resolution notes. " +
                    "Primary endpoint for the AI agent's resolve_ticket tool.")
    public ResponseEntity<TicketResponse> resolveTicket(
            @PathVariable Long id,
            @Valid @RequestBody ResolveTicketRequest request) {
        return ResponseEntity.ok(ticketService.resolveTicket(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket",
            description = "Deletes a ticket by its ID")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
