package com.splitwise.user.service;

import com.splitwise.user.dto.AuthResponse;
import com.splitwise.user.dto.LoginRequest;
import com.splitwise.user.dto.UserRequest;
import com.splitwise.user.dto.UserResponse;
import com.splitwise.user.exception.InvalidCredentialsException;
import com.splitwise.user.exception.ResourceNotFoundException;
import com.splitwise.user.exception.UserAlreadyExistsException;
import com.splitwise.user.model.RefreshToken;
import com.splitwise.user.model.User;
import com.splitwise.user.repository.UserRepository;
import com.splitwise.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User user;

    @BeforeEach
    void setUp() {
        userRequest = UserRequest.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .phone("+1234567890")
                .defaultCurrency("USD")
                .build();

        user = User.builder()
                .id("user123")
                .name("John Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .phone("+1234567890")
                .defaultCurrency("USD")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .emailVerified(false)
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyList())).thenReturn("test-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .id("rt1")
                .token("test-refresh-token")
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        // When
        AuthResponse response = userService.registerUser(userRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test-token", response.getToken());
        assertEquals("test-refresh-token", response.getRefreshToken());
        assertEquals(user.getId(), response.getUser().getId());
        assertEquals(user.getEmail(), response.getUser().getEmail());

        verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(userRequest.getPassword());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(userRequest);
        });

        verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_Success() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyList())).thenReturn("test-token");
        RefreshToken refreshToken = RefreshToken.builder()
                .id("rt1")
                .token("test-refresh-token")
                .expiryDate(Instant.now().plusSeconds(86400))
                .build();
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        // When
        AuthResponse response = userService.loginUser(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("test-refresh-token", response.getRefreshToken());
        assertEquals(user.getId(), response.getUser().getId());

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void loginUser_InvalidEmail() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(loginRequest);
        });

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void loginUser_InvalidPassword() {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("wrongpassword")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(loginRequest);
        });

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void loginUser_InactiveAccount() {
        // Given
        user.setActive(false);
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            userService.loginUser(loginRequest);
        });
    }

    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUserById("user123");

        // Then
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());

        verify(userRepository, times(1)).findById("user123");
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById("nonexistent");
        });

        verify(userRepository, times(1)).findById("nonexistent");
    }

    @Test
    void updateUser_Success() {
        // Given
        UserRequest updateRequest = UserRequest.builder()
                .name("Jane Doe")
                .phone("+9876543210")
                .defaultCurrency("EUR")
                .build();

        when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserResponse response = userService.updateUser("user123", updateRequest);

        // Then
        assertNotNull(response);
        verify(userRepository, times(1)).findById("user123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void addFriend_Success() {
        // Given
        User friend = User.builder()
                .id("friend123")
                .name("Friend Name")
                .email("friend@example.com")
                .build();

        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(userRepository.findById("friend123")).thenReturn(Optional.of(friend));

        // When
        userService.addFriend("user123", "friend123");

        // Then
        verify(userRepository, times(1)).findById("user123");
        verify(userRepository, times(1)).findById("friend123");
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void addFriend_UserNotFound() {
        // Given
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.addFriend("user123", "friend123");
        });
    }
}
