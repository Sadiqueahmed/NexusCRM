package com.nexuscrm.backend.exception;

/**
 * Exception thrown when a requested entity is not found in the database.
 * Handled globally by {@link GlobalExceptionHandler} to return HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String entityName, Long id) {
        super(String.format("%s not found with ID: %d", entityName, id));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
