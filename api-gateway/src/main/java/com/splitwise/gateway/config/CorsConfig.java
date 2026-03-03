package com.splitwise.gateway.config;

import org.springframework.context.annotation.Configuration;

// CORS is configured via spring.cloud.gateway.globalcors in application.yml
// Using the Gateway's built-in CORS support avoids conflicts with CorsWebFilter
@Configuration
public class CorsConfig {
        // CORS configuration is in application.yml under
        // spring.cloud.gateway.globalcors
}
