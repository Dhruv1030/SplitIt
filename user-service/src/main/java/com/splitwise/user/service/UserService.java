package com.splitwise.user.service;

import com.splitwise.user.dto.*;
import com.splitwise.user.model.User;
import com.splitwise.user.repository.UserRepository;
import com.splitwise.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse registerUser(UserRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .defaultCurrency(request.getDefaultCurrency() != null ? request.getDefaultCurrency() : "USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                Arrays.asList("ROLE_USER"));

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public AuthResponse loginUser(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        log.info("User logged in successfully: {}", user.getId());

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                Arrays.asList("ROLE_USER"));

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public UserResponse getUserById(String id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }

    public UserResponse updateUser(String id, UserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setDefaultCurrency(request.getDefaultCurrency());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return mapToUserResponse(user);
    }

    public List<UserResponse> getFriends(String userId) {
        log.info("Fetching friends for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getFriendIds().stream()
                .map(friendId -> userRepository.findById(friendId)
                        .map(this::mapToUserResponse)
                        .orElse(null))
                .filter(friend -> friend != null)
                .collect(Collectors.toList());
    }

    public void addFriend(String userId, String friendId) {
        log.info("Adding friend {} to user {}", friendId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        if (!user.getFriendIds().contains(friendId)) {
            user.getFriendIds().add(friendId);
            userRepository.save(user);
        }

        // Add bidirectional friendship
        if (!friend.getFriendIds().contains(userId)) {
            friend.getFriendIds().add(userId);
            userRepository.save(friend);
        }

        log.info("Friend added successfully");
    }

    public void removeFriend(String userId, String friendId) {
        log.info("Removing friend {} from user {}", friendId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getFriendIds().remove(friendId);
        userRepository.save(user);

        // Remove bidirectional friendship
        userRepository.findById(friendId).ifPresent(friend -> {
            friend.getFriendIds().remove(userId);
            userRepository.save(friend);
        });

        log.info("Friend removed successfully");
    }

    public List<UserResponse> searchUsers(String query) {
        log.info("Searching users with query: {}", query);
        // Simple implementation - in production, use MongoDB text search
        return userRepository.findAll().stream()
                .filter(user -> user.getName().toLowerCase().contains(query.toLowerCase()) ||
                        user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .friendIds(user.getFriendIds())
                .defaultCurrency(user.getDefaultCurrency())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
