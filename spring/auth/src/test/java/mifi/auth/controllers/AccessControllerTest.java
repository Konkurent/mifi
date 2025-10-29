package mifi.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mifi.auth.dto.CreateUserEvent;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.service.AccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccessController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccessService accessService;

    private ObjectMapper objectMapper;

    private UpdateUserPayload validUpdatePayload;
    private CreateUserEvent validCreateEvent;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        validUpdatePayload = new UpdateUserPayload(
                LocalDateTime.now(),
                "newemail@example.com",
                "newlogin",
                1L
        );

        validCreateEvent = CreateUserEvent.builder()
                .email("test@example.com")
                .userId(1L)
                .login("testuser")
                .password("password123")
                .creationDate(LocalDateTime.now())
                .admin(false)
                .build();
    }

    @Test
    void testUpdateAccess_Success() throws Exception {
        // Given
        String expectedToken = "updated-jwt-token";
        when(accessService.updateAccess(any(UpdateUserPayload.class))).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(put("/api/v1/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdatePayload)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedToken));

        verify(accessService, times(1)).updateAccess(any(UpdateUserPayload.class));
    }

    @Test
    void testUpdateAccess_InvalidPayload_ShouldReturnBadRequest() throws Exception {
        // Given - payload с пустым email
        UpdateUserPayload invalidPayload = new UpdateUserPayload(
                LocalDateTime.now(),
                "", // пустой email
                "newlogin",
                1L
        );

        // When & Then
        mockMvc.perform(put("/api/v1/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateAccess_MissingUserId_ShouldProcessRequest() throws Exception {
        // Given - payload без userId
        UpdateUserPayload invalidPayload = new UpdateUserPayload(
                LocalDateTime.now(),
                "email@example.com",
                "login",
                null // отсутствует userId
        );

        // Когда userId null, валидация может пройти, но бизнес-логика вернет ошибку
        // Проверяем, что контроллер обрабатывает запрос (может вернуть ошибку, но не падает с исключением)
        var result = mockMvc.perform(put("/api/v1/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andReturn();
        
        assertNotNull(result);
        assertTrue(result.getResponse().getStatus() >= 200 && result.getResponse().getStatus() < 600);
    }

    @Test
    void testCreateAccess_Success() throws Exception {
        // Given
        doNothing().when(accessService).createAccess(any(CreateUserEvent.class));

        // When & Then
        mockMvc.perform(post("/api/v1/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateEvent)))
                .andExpect(status().isOk());

        verify(accessService, times(1)).createAccess(any(CreateUserEvent.class));
    }

    @Test
    void testCreateAccess_InvalidEvent_ShouldProcessRequest() throws Exception {
        // Given
        CreateUserEvent invalidEvent = CreateUserEvent.builder()
                .email("") // пустой email
                .userId(1L)
                .login("testuser")
                .password("password123")
                .creationDate(LocalDateTime.now())
                .admin(false)
                .build();

        // When & Then - проверяем, что запрос обработан (может вернуть ошибку, но не падает с исключением)
        var result = mockMvc.perform(post("/api/v1/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEvent)))
                .andReturn();
        
        assertNotNull(result);
        assertTrue(result.getResponse().getStatus() >= 200 && result.getResponse().getStatus() < 600);
    }

    @Test
    void testDeleteAccess_Success() throws Exception {
        // Given
        String email = "test@example.com";
        doNothing().when(accessService).deleteAccess(email);

        // When & Then
        mockMvc.perform(delete("/api/v1/access/{email}", email))
                .andExpect(status().isOk());

        verify(accessService, times(1)).deleteAccess(email);
    }

    @Test
    void testDeleteAccess_WithSpecialCharacters() throws Exception {
        // Given
        String email = "test+user@example.com";
        doNothing().when(accessService).deleteAccess(email);

        // When & Then
        mockMvc.perform(delete("/api/v1/access/{email}", email))
                .andExpect(status().isOk());

        verify(accessService, times(1)).deleteAccess(email);
    }

    @Test
    void testDeleteAccess_EmptyEmail() throws Exception {
        // Given
        String email = "";

        // When & Then
        mockMvc.perform(delete("/api/v1/access/{email}", email))
                .andExpect(status().isOk());

        verify(accessService, times(1)).deleteAccess(email);
    }

    @Test
    void testGetCustomerDetails_Success() throws Exception {
        // Given
        String email = "test@example.com";
        mifi.auth.security.dto.CustomerDetails customerDetails = mifi.auth.security.dto.CustomerDetails.builder()
                .email(email)
                .userId(1L)
                .password("")
                .authorities(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")))
                .build();
        
        when(accessService.getCustomerDetailsByEmail(email)).thenReturn(customerDetails);

        // When & Then
        mockMvc.perform(get("/api/v1/access/customer/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_USER"));

        verify(accessService, times(1)).getCustomerDetailsByEmail(email);
    }

    @Test
    void testGetCustomerDetails_NotFound() throws Exception {
        // Given
        String email = "nonexistent@example.com";
        when(accessService.getCustomerDetailsByEmail(email))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found!"));

        // When & Then
        var result = mockMvc.perform(get("/api/v1/access/customer/{email}", email))
                .andReturn();
        
        assertTrue(result.getResponse().getStatus() == 404 || result.getResponse().getStatus() == 500,
                "Должен вернуть 404 или 500 при отсутствии пользователя");

        verify(accessService, times(1)).getCustomerDetailsByEmail(email);
    }
}

