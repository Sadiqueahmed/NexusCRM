package com.nexuscrm.backend.service;

import com.nexuscrm.backend.dto.*;
import com.nexuscrm.backend.entity.User;
import com.nexuscrm.backend.exception.ResourceNotFoundException;
import com.nexuscrm.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for User CRUD operations.
 * Handles business logic and entity-to-DTO mapping.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users in the system.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single user by ID.
     *
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toResponse(user);
    }

    /**
     * Creates a new user.
     * In a production system, the password would be bcrypt-hashed here.
     */
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(request.getPassword()) // TODO: bcrypt hash in production
                .role(request.getRole() != null ? request.getRole() : "AGENT")
                .build();

        User saved = userRepository.save(user);
        log.info("User created with ID: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Updates an existing user.
     *
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponse updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getUsername() != null) user.setUsername(request.getUsername());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getRole() != null) user.setRole(request.getRole());

        User saved = userRepository.save(user);
        log.info("User updated: ID={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Deletes a user by ID.
     *
     * @throws ResourceNotFoundException if user not found
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
        log.info("User deleted: ID={}", id);
    }

    // =========================================================================
    // Private Helpers
    // =========================================================================

    /**
     * Maps a User entity to a UserResponse DTO.
     * Never exposes the password hash.
     */
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
