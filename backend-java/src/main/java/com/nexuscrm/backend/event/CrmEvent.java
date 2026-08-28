package com.nexuscrm.backend.event;

import lombok.*;
import java.time.OffsetDateTime;

/**
 * Represents a CRM event to be broadcast via Server-Sent Events (SSE).
 * Events are published whenever the AI agent or a user modifies CRM data,
 * allowing the React dashboard to update in real-time.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CrmEvent {

    /**
     * Event type discriminator for SSE consumers.
     * Values: LEAD_UPDATED, TICKET_RESOLVED, TICKET_CREATED, AI_ACTION
     */
    private String type;

    /** The entity type affected (LEAD, TICKET, USER) */
    private String entity;

    /** The ID of the affected entity */
    private Long entityId;

    /** Human-readable summary of what happened */
    private String message;

    /** Additional structured data about the event */
    private Object details;

    /** Whether this action was performed by the AI agent */
    @Builder.Default
    private boolean aiGenerated = false;

    /** Timestamp of the event */
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();
}
