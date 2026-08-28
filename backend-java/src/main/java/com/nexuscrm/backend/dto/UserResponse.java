package com.nexuscrm.backend.dto;

import lombok.*;
import java.time.OffsetDateTime;

/**
 * Response DTO for User data. Never exposes password hashes.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
