package com.splitwise.group.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://USER-SERVICE}")
    private String userServiceUrl;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserDTO getUserById(String userId) {
        log.debug("Fetching user details for userId: {}", userId);
        String url = userServiceUrl + "/api/users/" + userId;
        return restTemplate.getForObject(url, UserDTO.class);
    }

    public UserDTO getUserByIdFallback(String userId, Throwable t) {
        log.warn("Circuit breaker fallback for getUserById({}): {}", userId, t.getMessage());
        return null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private String id;
        private String name;
        private String email;
        private String phone;
    }
}
