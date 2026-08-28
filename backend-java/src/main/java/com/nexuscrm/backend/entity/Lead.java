package com.nexuscrm.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

/**
 * JPA Entity representing a Sales Lead in the CRM pipeline.
 * Tracks prospective customers from initial contact through conversion.
 * Maps to the "leads" table in PostgreSQL.
 */
@Entity
@Table(name = "leads")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 200)
    private String company;

    /** Pipeline status: NEW, CONTACTED, QUALIFIED, LOST, CONVERTED */
    @Column(length = 50)
    @Builder.Default
    private String status = "NEW";

    /** Lead acquisition source: WEBSITE, REFERRAL, AI_GENERATED, etc. */
    @Column(length = 100)
    private String source;

    /** FK to the assigned CRM agent */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** AI-generated intelligence summary about this lead */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

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
