package com.nexuscrm.backend.repository;

import com.nexuscrm.backend.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Lead entities.
 * Supports filtering by pipeline status and assigned agent.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    List<Lead> findByStatus(String status);

    List<Lead> findByAssignedToId(Long userId);

    List<Lead> findByStatusAndAssignedToId(String status, Long userId);

    long countByStatus(String status);
}
