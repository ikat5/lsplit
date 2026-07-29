package com.lsplit.service;

import com.lsplit.config.JwtUtil;
import com.lsplit.dto.request.LoginRequest;
import com.lsplit.dto.request.RegisterRequest;
import com.lsplit.dto.response.AuthResponse;
import com.lsplit.entity.User;
import com.lsplit.exception.BadRequestException;
import com.lsplit.mapper.UserMapper;
import com.lsplit.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    // ---------------------------------------------------------------
    // register tests
    // ---------------------------------------------------------------

    @Test
    void testRegister_success() {
        UUID generatedId = UUID.randomUUID();

        RegisterRequest req = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pw");

        // Simulate JPA ID generation by mutating the saved user
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(generatedId);
            return u;
        }).when(userRepository).save(any(User.class));

        when(jwtUtil.generateToken("alice@example.com", generatedId)).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(generatedId);
        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void testRegister_emailAlreadyTaken_throwsBadRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already in use");
    }

    // ---------------------------------------------------------------
    // login tests
    // ---------------------------------------------------------------

    @Test
    void testLogin_success() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .name("Bob")
                .email("bob@example.com")
                .passwordHash("hashed_pw")
                .build();

        LoginRequest req = LoginRequest.builder()
                .email("bob@example.com")
                .password("secret123")
                .build();

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "hashed_pw")).thenReturn(true);
        when(jwtUtil.generateToken("bob@example.com", userId)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getName()).isEqualTo("Bob");
        assertThat(response.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void testLogin_invalidPassword_throwsBadRequest() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@example.com")
                .passwordHash("hashed_pw")
                .build();

        LoginRequest req = LoginRequest.builder()
                .email("bob@example.com")
                .password("wrong_password")
                .build();

        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong_password", "hashed_pw")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid credentials");
    }
}
