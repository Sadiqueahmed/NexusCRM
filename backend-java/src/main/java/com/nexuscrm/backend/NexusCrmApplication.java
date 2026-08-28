package com.nexuscrm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NexusCRM Backend Application
 * 
 * Core CRM API server providing REST endpoints for Users, Leads, and Tickets,
 * plus Server-Sent Events (SSE) for real-time AI action broadcasting.
 */
@SpringBootApplication
public class NexusCrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusCrmApplication.class, args);
    }
}
