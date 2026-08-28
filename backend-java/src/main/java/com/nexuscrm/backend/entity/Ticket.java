package com.nexuscrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * JPA Entity representing a Customer Support Ticket.
 * Tracks support requests from creation through resolution, including
 * whether the ticket was handled autonomously by the AI agent.
 * Maps to the "tickets" table in PostgreSQL.
 */
@Entity
@Table(name = "tickets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Ticket lifecycle: OPEN, IN_PROGRESS, RESOLVED, CLOSED */
    @Column(length = 50)
    @Builder.Default
    private String status = "OPEN";

    /** Urgency level: LOW, MEDIUM, HIGH, CRITICAL */
    @Column(length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(length = 100)
    private String category;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    /** FK to the assigned support agent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /** Human or AI-generated resolution notes */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    /** Flags whether this ticket was resolved by the autonomous AI agent */
    @Column(name = "ai_handled")
    @Builder.Default
    private Boolean aiHandled = false;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
