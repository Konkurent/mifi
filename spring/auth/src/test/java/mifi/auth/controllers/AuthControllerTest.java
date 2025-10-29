package mifi.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import mifi.auth.dto.SignUpPayload;
import mifi.auth.service.AccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessService accessService;

    @Autowired
    private ObjectMapper objectMapper;

    private SignUpPayload validSignUpPayload;

    @BeforeEach
    void setUp() {
        validSignUpPayload = new SignUpPayload(
                "John",
                "Middle",
                "Doe",
                "test@example.com",
                "testuser",
                "password123"
        );
    }

    @Test
    void testSignUp_Success() throws Exception {
        // Given
        String expectedToken = "test-jwt-token";
        when(accessService.signUp(any(SignUpPayload.class))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validSignUpPayload)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));
    }

    @Test
    void testSignUp_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        SignUpPayload invalidPayload = new SignUpPayload(
                "John",
                "Middle",
                "Doe",
                "invalid-email", // невалидный email
                "testuser",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_MissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        // Given - payload без обязательных полей
        SignUpPayload invalidPayload = new SignUpPayload(
                "", // пустое firstName
                "Middle",
                "", // пустое lastName
                "test@example.com",
                "testuser",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_MissingEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        SignUpPayload invalidPayload = new SignUpPayload(
                "John",
                "Middle",
                "Doe",
                "", // пустой email
                "testuser",
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_MissingPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        SignUpPayload invalidPayload = new SignUpPayload(
                "John",
                "Middle",
                "Doe",
                "test@example.com",
                "testuser",
                "" // пустой пароль
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSignUp_NullPayload_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}

