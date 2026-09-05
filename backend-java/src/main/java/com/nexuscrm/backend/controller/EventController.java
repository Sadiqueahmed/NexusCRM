package com.nexuscrm.backend.controller;

import com.nexuscrm.backend.event.SseEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE Event Stream Controller.
 * Provides a long-lived SSE connection for the React frontend
 * to receive real-time CRM events (lead updates, ticket resolutions,
 * AI agent actions) as they happen.
 */
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Server-Sent Events for real-time CRM updates")
public class EventController {

    private final SseEventPublisher sseEventPublisher;

    public EventController(SseEventPublisher sseEventPublisher) {
        this.sseEventPublisher = sseEventPublisher;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to SSE event stream",
            description = "Opens a long-lived SSE connection. Events are pushed whenever " +
                    "CRM data is modified (by humans or the AI agent). " +
                    "Event types: LEAD_UPDATED, TICKET_CREATED, TICKET_RESOLVED, AI_ACTION")
    public SseEmitter stream() {
        return sseEventPublisher.subscribe();
    }
}
