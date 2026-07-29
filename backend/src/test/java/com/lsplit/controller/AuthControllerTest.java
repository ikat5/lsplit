package com.lsplit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsplit.config.JwtUtil;
import com.lsplit.config.UserDetailsServiceImpl;
import com.lsplit.dto.request.LoginRequest;
import com.lsplit.dto.request.RegisterRequest;
import com.lsplit.dto.response.AuthResponse;
import com.lsplit.service.AuthService;
import com.lsplit.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer test for AuthController.
 *
 * Spring Security auto-configuration is excluded so that the controller is
 * tested in isolation without JWT filter concerns.  Validation and JSON
 * serialization remain active via the standard MVC slice.
 */
@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    /** Required by JwtAuthFilter (a @Component Filter loaded by the web slice). */
    @MockBean
    private JwtUtil jwtUtil;

    /** Required by JwtAuthFilter. */
    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ---------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------

    @Test
    void testRegister_validRequest_returns201() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .name("Alice")
                .email("alice@example.com")
                .phone("1234567890")
                .password("password123")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .token("jwt-token")
                .userId(UUID.randomUUID())
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void testLogin_validCredentials_returns200() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("alice@example.com")
                .password("password123")
                .build();

        AuthResponse mockResponse = AuthResponse.builder()
                .token("jwt-token")
                .userId(UUID.randomUUID())
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void testRegister_blankName_returns400() throws Exception {
        // name is blank — fails @NotBlank validation → 400
        RegisterRequest req = RegisterRequest.builder()
                .name("")
                .email("alice@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
