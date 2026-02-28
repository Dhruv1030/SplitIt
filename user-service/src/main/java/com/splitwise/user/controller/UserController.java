package com.splitwise.user.controller;

import com.splitwise.user.dto.*;
import com.splitwise.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user registration, authentication, and profile management")
public class UserController {

        private final UserService userService;

        @Operation(summary = "Register a new user", description = "Create a new user account with email and password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "409", description = "User already exists")
        })
        @PostMapping("/register")
        public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRequest request) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(userService.registerUser(request));
        }

        @Operation(summary = "User login", description = "Authenticate user and return JWT token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        @PostMapping("/login")
        public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
                return ResponseEntity.ok(userService.loginUser(request));
        }

        @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Invalid refresh token")
        })
        @PostMapping("/refresh-token")
        public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
                return ResponseEntity.ok(userService.refreshToken(request));
        }

        @Operation(summary = "Logout user", description = "Logout user and invalidate refresh token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Log out successful"),
        })
        @PostMapping("/logout")
        public ResponseEntity<Void> logout(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
                userService.logout(userId);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(
                        @Parameter(description = "User ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(userService.getUserById(id));
        }

        @Operation(summary = "Update user profile", description = "Update user information", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data")
        })
        @PutMapping("/{id}")
        public ResponseEntity<UserResponse> updateUser(
                        @Parameter(description = "User ID", required = true) @PathVariable String id,
                        @Valid @RequestBody UserRequest request) {
                return ResponseEntity.ok(userService.updateUser(id, request));
        }

        @Operation(summary = "Get user's friends", description = "Retrieve list of user's friends", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Friends list retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/{id}/friends")
        public ResponseEntity<List<UserResponse>> getFriends(
                        @Parameter(description = "User ID", required = true) @PathVariable String id) {
                return ResponseEntity.ok(userService.getFriends(id));
        }

        @Operation(summary = "Add friend", description = "Add a user to friends list", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Friend added successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Cannot add self as friend")
        })
        @PostMapping("/{id}/friends")
        public ResponseEntity<Void> addFriend(
                        @Parameter(description = "User ID", required = true) @PathVariable String id,
                        @Parameter(description = "Friend's User ID", required = true) @RequestParam String friendId) {
                userService.addFriend(id, friendId);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Remove friend", description = "Remove a user from friends list", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Friend removed successfully"),
                        @ApiResponse(responseCode = "404", description = "User or friend not found")
        })
        @DeleteMapping("/{id}/friends/{friendId}")
        public ResponseEntity<Void> removeFriend(
                        @Parameter(description = "User ID", required = true) @PathVariable String id,
                        @Parameter(description = "Friend's User ID to remove", required = true) @PathVariable String friendId) {
                userService.removeFriend(id, friendId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/me")
        public ResponseEntity<UserResponse> getCurrentUser(
                        @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
                return ResponseEntity.ok(userService.getUserById(userId));
        }

        @Operation(summary = "Search users", description = "Search users by name or email", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully")
        })
        @GetMapping("/search")
        public ResponseEntity<List<UserResponse>> searchUsers(
                        @Parameter(description = "Search query (name or email)", required = true) @RequestParam String query) {
                return ResponseEntity.ok(userService.searchUsers(query));
        }
}
