package com.nexuscrm.backend.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SSE Event Publisher — manages active SSE connections and broadcasts
 * CRM events to all connected frontend clients in real-time.
 *
 * Thread-safe: uses CopyOnWriteArrayList for concurrent access.
 * Dead connections are automatically cleaned up on send failure.
 */
@Component
public class SseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SseEventPublisher.class);

    /** Thread-safe list of active SSE connections */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private final ObjectMapper objectMapper;

    public SseEventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Registers a new SSE client connection.
     * Sets a 30-minute timeout and auto-cleanup callbacks.
     *
     * @return the SseEmitter for the new connection
     */
    public SseEmitter subscribe() {
        // 30-minute timeout for long-lived dashboard connections
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Auto-remove on completion, timeout, or error
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE client disconnected. Active connections: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE client timed out. Active connections: {}", emitters.size());
        });
        emitter.onError(e -> {
            emitters.remove(emitter);
            log.debug("SSE client error. Active connections: {}", emitters.size());
        });

        emitters.add(emitter);
        log.info("New SSE client connected. Active connections: {}", emitters.size());

        return emitter;
    }

    /**
     * Broadcasts a CRM event to all connected SSE clients.
     * Failed sends (disconnected clients) are automatically cleaned up.
     *
     * @param event the CRM event to broadcast
     */
    public void publish(CrmEvent event) {
        log.info("Publishing SSE event: type={}, entity={}, entityId={}",
                event.getType(), event.getEntity(), event.getEntityId());

        List<SseEmitter> deadEmitters = new java.util.ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                String jsonData = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event()
                        .name(event.getType())
                        .data(jsonData));
            } catch (IOException e) {
                deadEmitters.add(emitter);
                log.debug("Failed to send SSE event to client, marking for removal");
            }
        }

        // Clean up dead connections
        emitters.removeAll(deadEmitters);
        if (!deadEmitters.isEmpty()) {
            log.debug("Removed {} dead SSE connections. Active: {}",
                    deadEmitters.size(), emitters.size());
        }
    }

    /**
     * Returns the number of currently active SSE connections.
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
