package com.splitwise.group.client;

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

    public UserDTO getUserById(String userId) {
        try {
            log.debug("Fetching user details for userId: {}", userId);
            String url = userServiceUrl + "/api/users/" + userId;
            return restTemplate.getForObject(url, UserDTO.class);
        } catch (Exception e) {
            log.warn("Failed to fetch user details for userId: {}. Error: {}", userId, e.getMessage());
            // Return null if user service is unavailable or user not found
            return null;
        }
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
