package com.nexuscrm.backend.repository;

import com.nexuscrm.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Ticket entities.
 * Supports filtering by status, priority, and AI-handled flag.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStatus(String status);

    List<Ticket> findByPriority(String priority);

    List<Ticket> findByStatusAndPriority(String status, String priority);

    List<Ticket> findByAssignedToId(Long userId);

    List<Ticket> findByAiHandled(Boolean aiHandled);

    List<Ticket> findByCustomerEmail(String customerEmail);

    long countByStatus(String status);

    long countByAiHandled(Boolean aiHandled);
}
