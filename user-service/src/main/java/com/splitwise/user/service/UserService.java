package com.splitwise.user.service;

import com.splitwise.user.dto.*;
import com.splitwise.user.exception.InvalidCredentialsException;
import com.splitwise.user.exception.ResourceNotFoundException;
import com.splitwise.user.exception.UserAlreadyExistsException;
import com.splitwise.user.model.FriendRequest;
import com.splitwise.user.model.FriendRequestStatus;
import com.splitwise.user.model.User;
import com.splitwise.user.repository.FriendRequestRepository;
import com.splitwise.user.repository.UserRepository;
import com.splitwise.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

        private final UserRepository userRepository;
        private final FriendRequestRepository friendRequestRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final RefreshTokenService refreshTokenService;
        private final RestTemplate restTemplate;

        @Value("${app.notification-service-url}")
        private String notificationServiceUrl;

        @Value("${app.base-url}")
        private String baseUrl;

        public AuthResponse registerUser(UserRequest request) {
                log.info("Registering user with email: {}", request.getEmail());

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new UserAlreadyExistsException("Email already exists");
                }

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .phone(request.getPhone())
                                .defaultCurrency(request.getDefaultCurrency() != null ? request.getDefaultCurrency()
                                                : "USD")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .isActive(true)
                                .emailVerified(false)
                                .roles(new java.util.HashSet<>(Arrays.asList(com.splitwise.user.model.Role.ROLE_USER)))
                                .build();

                user = userRepository.save(user);
                log.info("User registered successfully with id: {}", user.getId());

                String token = jwtTokenProvider.generateToken(
                                user.getId(),
                                user.getEmail(),
                                Arrays.asList("ROLE_USER"));

                com.splitwise.user.model.RefreshToken refreshToken = refreshTokenService
                                .createRefreshToken(user.getId());

                return AuthResponse.builder()
                                .token(token)
                                .refreshToken(refreshToken.getToken())
                                .user(mapToUserResponse(user))
                                .build();
        }

        public AuthResponse loginUser(LoginRequest request) {
                log.info("User login attempt: {}", request.getEmail());

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new InvalidCredentialsException("Invalid email or password");
                }

                if (!user.isActive()) {
                        throw new InvalidCredentialsException("User account is deactivated");
                }

                log.info("User logged in successfully: {}", user.getId());

                List<String> roles = user.getRoles().stream()
                                .map(Enum::name)
                                .collect(Collectors.toList());

                String token = jwtTokenProvider.generateToken(
                                user.getId(),
                                user.getEmail(),
                                roles);

                com.splitwise.user.model.RefreshToken refreshToken = refreshTokenService
                                .createRefreshToken(user.getId());

                return AuthResponse.builder()
                                .token(token)
                                .refreshToken(refreshToken.getToken())
                                .user(mapToUserResponse(user))
                                .build();
        }

        public UserResponse getUserById(String id) {
                log.info("Fetching user with id: {}", id);
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                return mapToUserResponse(user);
        }

        public UserResponse updateUser(String id, UserRequest request) {
                log.info("Updating user with id: {}", id);

                User user = userRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                User friend = userRepository.findById(friendId)
                                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

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
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                user.getFriendIds().remove(friendId);
                userRepository.save(user);

                // Remove bidirectional friendship
                userRepository.findById(friendId).ifPresent(friend -> {
                        friend.getFriendIds().remove(userId);
                        userRepository.save(friend);
                });

                log.info("Friend removed successfully");
        }

        // ==================== Friend Request Methods ====================

        public FriendRequestDTO sendFriendRequest(String senderId, SendFriendRequestDTO request) {
                log.info("Sending friend request from user {}", senderId);

                User sender = userRepository.findById(senderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

                // Resolve receiver by email or ID
                User receiver;
                if (request.getReceiverEmail() != null && !request.getReceiverEmail().isBlank()) {
                        receiver = userRepository.findByEmail(request.getReceiverEmail())
                                        .orElseThrow(() -> new ResourceNotFoundException("User with email " + request.getReceiverEmail() + " not found"));
                } else if (request.getReceiverId() != null && !request.getReceiverId().isBlank()) {
                        receiver = userRepository.findById(request.getReceiverId())
                                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                } else {
                        throw new IllegalArgumentException("Either receiverEmail or receiverId must be provided");
                }

                // Validations
                if (senderId.equals(receiver.getId())) {
                        throw new IllegalArgumentException("Cannot send friend request to yourself");
                }

                if (sender.getFriendIds().contains(receiver.getId())) {
                        throw new IllegalArgumentException("You are already friends with this user");
                }

                // Check for existing pending request in either direction
                Optional<FriendRequest> existingRequest = friendRequestRepository
                                .findBySenderIdAndReceiverIdAndStatus(senderId, receiver.getId(), FriendRequestStatus.PENDING);
                if (existingRequest.isPresent()) {
                        throw new IllegalArgumentException("Friend request already sent");
                }

                Optional<FriendRequest> reverseRequest = friendRequestRepository
                                .findBySenderIdAndReceiverIdAndStatus(receiver.getId(), senderId, FriendRequestStatus.PENDING);
                if (reverseRequest.isPresent()) {
                        throw new IllegalArgumentException("This user has already sent you a friend request. Check your pending requests.");
                }

                // Create friend request
                FriendRequest friendRequest = FriendRequest.builder()
                                .senderId(senderId)
                                .receiverId(receiver.getId())
                                .build();

                friendRequest = friendRequestRepository.save(friendRequest);
                log.info("Friend request created with id: {}", friendRequest.getId());

                // Send email notification
                sendFriendRequestEmail(sender, receiver, friendRequest.getToken());

                return mapToFriendRequestDTO(friendRequest, sender, receiver);
        }

        public FriendRequestDTO acceptFriendRequest(String requestId, String userId) {
                log.info("Accepting friend request {} by user {}", requestId, userId);

                FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

                if (!friendRequest.getReceiverId().equals(userId)) {
                        throw new IllegalArgumentException("Only the receiver can accept this request");
                }

                if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
                        throw new IllegalArgumentException("This request has already been " + friendRequest.getStatus().name().toLowerCase());
                }

                friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
                friendRequest.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(friendRequest);

                // Add bidirectional friendship
                addFriend(friendRequest.getSenderId(), friendRequest.getReceiverId());

                User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
                User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

                log.info("Friend request accepted, friendship established");
                return mapToFriendRequestDTO(friendRequest, sender, receiver);
        }

        public FriendRequestDTO declineFriendRequest(String requestId, String userId) {
                log.info("Declining friend request {} by user {}", requestId, userId);

                FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                                .orElseThrow(() -> new ResourceNotFoundException("Friend request not found"));

                if (!friendRequest.getReceiverId().equals(userId)) {
                        throw new IllegalArgumentException("Only the receiver can decline this request");
                }

                if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
                        throw new IllegalArgumentException("This request has already been " + friendRequest.getStatus().name().toLowerCase());
                }

                friendRequest.setStatus(FriendRequestStatus.DECLINED);
                friendRequest.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(friendRequest);

                User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
                User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

                log.info("Friend request declined");
                return mapToFriendRequestDTO(friendRequest, sender, receiver);
        }

        public FriendRequestDTO acceptFriendRequestByToken(String token) {
                log.info("Accepting friend request via email token");

                FriendRequest friendRequest = friendRequestRepository.findByToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired friend request link"));

                if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
                        throw new IllegalArgumentException("This request has already been " + friendRequest.getStatus().name().toLowerCase());
                }

                friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
                friendRequest.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(friendRequest);

                // Add bidirectional friendship
                addFriend(friendRequest.getSenderId(), friendRequest.getReceiverId());

                User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
                User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

                log.info("Friend request accepted via token, friendship established");
                return mapToFriendRequestDTO(friendRequest, sender, receiver);
        }

        public FriendRequestDTO declineFriendRequestByToken(String token) {
                log.info("Declining friend request via email token");

                FriendRequest friendRequest = friendRequestRepository.findByToken(token)
                                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired friend request link"));

                if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
                        throw new IllegalArgumentException("This request has already been " + friendRequest.getStatus().name().toLowerCase());
                }

                friendRequest.setStatus(FriendRequestStatus.DECLINED);
                friendRequest.setUpdatedAt(LocalDateTime.now());
                friendRequestRepository.save(friendRequest);

                User sender = userRepository.findById(friendRequest.getSenderId()).orElse(null);
                User receiver = userRepository.findById(friendRequest.getReceiverId()).orElse(null);

                log.info("Friend request declined via token");
                return mapToFriendRequestDTO(friendRequest, sender, receiver);
        }

        public List<FriendRequestDTO> getPendingRequests(String userId) {
                log.info("Fetching pending friend requests for user: {}", userId);
                return friendRequestRepository.findByReceiverIdAndStatus(userId, FriendRequestStatus.PENDING)
                                .stream()
                                .map(req -> {
                                        User sender = userRepository.findById(req.getSenderId()).orElse(null);
                                        User receiver = userRepository.findById(req.getReceiverId()).orElse(null);
                                        return mapToFriendRequestDTO(req, sender, receiver);
                                })
                                .collect(Collectors.toList());
        }

        public List<FriendRequestDTO> getSentRequests(String userId) {
                log.info("Fetching sent friend requests for user: {}", userId);
                return friendRequestRepository.findBySenderIdAndStatus(userId, FriendRequestStatus.PENDING)
                                .stream()
                                .map(req -> {
                                        User sender = userRepository.findById(req.getSenderId()).orElse(null);
                                        User receiver = userRepository.findById(req.getReceiverId()).orElse(null);
                                        return mapToFriendRequestDTO(req, sender, receiver);
                                })
                                .collect(Collectors.toList());
        }

        private void sendFriendRequestEmail(User sender, User receiver, String token) {
                try {
                        String url = notificationServiceUrl + "/api/notifications/friend-request";

                        Map<String, String> emailRequest = new HashMap<>();
                        emailRequest.put("receiverEmail", receiver.getEmail());
                        emailRequest.put("receiverName", receiver.getName());
                        emailRequest.put("senderName", sender.getName());
                        emailRequest.put("senderEmail", sender.getEmail());
                        emailRequest.put("acceptUrl", baseUrl + "/api/users/friend-requests/accept?token=" + token);
                        emailRequest.put("declineUrl", baseUrl + "/api/users/friend-requests/decline?token=" + token);

                        restTemplate.postForEntity(url, emailRequest, String.class);
                        log.info("Friend request email sent to {}", receiver.getEmail());
                } catch (Exception e) {
                        log.error("Failed to send friend request email to {}: {}", receiver.getEmail(), e.getMessage());
                        // Don't fail the request if email fails
                }
        }

        // ==================== Existing Methods ====================

        public List<UserResponse> searchUsers(String query) {
                log.info("Searching users with query: {}", query);
                // Simple implementation - in production, use MongoDB text search
                return userRepository.findAll().stream()
                                .filter(user -> user.getName().toLowerCase().contains(query.toLowerCase()) ||
                                                user.getEmail().toLowerCase().contains(query.toLowerCase()))
                                .map(this::mapToUserResponse)
                                .collect(Collectors.toList());
        }

        public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
                String requestRefreshToken = request.getRefreshToken();

                return refreshTokenService.findByToken(requestRefreshToken)
                                .map(refreshTokenService::verifyExpiration)
                                .map(com.splitwise.user.model.RefreshToken::getUser)
                                .map(user -> {
                                        List<String> roles = user.getRoles().stream()
                                                        .map(Enum::name)
                                                        .collect(Collectors.toList());
                                        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(),
                                                        roles);
                                        return new TokenRefreshResponse(token, requestRefreshToken);
                                })
                                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
        }

        public void logout(String userId) {
                refreshTokenService.deleteByUserId(userId);
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

        private FriendRequestDTO mapToFriendRequestDTO(FriendRequest request, User sender, User receiver) {
                return FriendRequestDTO.builder()
                                .id(request.getId())
                                .senderId(request.getSenderId())
                                .senderName(sender != null ? sender.getName() : null)
                                .senderEmail(sender != null ? sender.getEmail() : null)
                                .receiverId(request.getReceiverId())
                                .receiverName(receiver != null ? receiver.getName() : null)
                                .receiverEmail(receiver != null ? receiver.getEmail() : null)
                                .status(request.getStatus().name())
                                .createdAt(request.getCreatedAt())
                                .build();
        }
}
