package com.nexuscrm.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the NexusCRM API.
 * Allows cross-origin requests from the React frontend (port 3000)
 * and the Python AI service (port 8000) during development.
 *
 * In production, restrict allowed origins to the actual deployment domains.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",   // React dev server
                        "http://localhost:5173",   // Vite dev server
                        "http://frontend:80",      // Docker internal
                        "http://localhost:8000"     // AI service
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
