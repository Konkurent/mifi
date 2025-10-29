package mifi.auth.service;

import mifi.auth.dao.entities.AccessEntity;
import mifi.auth.dao.repositories.AccessRepository;
import mifi.auth.dto.CreateUserEvent;
import mifi.auth.dto.SignUpPayload;
import mifi.auth.dto.UpdateUserPayload;
import mifi.auth.dto.User;
import mifi.auth.exception.AccessServiceException;
import mifi.auth.controllers.advise.SystemCode;
import mifi.auth.security.dto.CustomerDetails;
import mifi.auth.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessServiceTest {

    @Mock
    private AccessRepository accessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccessService accessService;

    private AccessEntity testAccessEntity;
    private User testUser;
    private SignUpPayload testSignUpPayload;
    private CreateUserEvent testCreateUserEvent;
    private UpdateUserPayload testUpdateUserPayload;

    @BeforeEach
    void setUp() {
        testAccessEntity = AccessEntity.builder()
                .email("test@example.com")
                .userId(1L)
                .login("testuser")
                .role("USER")
                .password("encodedPassword")
                .build();

        testUser = new User(1L, "John", "Doe", "Middle", "test@example.com", "testuser");

        testSignUpPayload = new SignUpPayload(
                "John",
                "Middle",
                "Doe",
                "test@example.com",
                "testuser",
                "password123"
        );

        testCreateUserEvent = CreateUserEvent.builder()
                .email("test@example.com")
                .userId(1L)
                .login("testuser")
                .password("password123")
                .creationDate(LocalDateTime.now())
                .admin(false)
                .build();

        testUpdateUserPayload = new UpdateUserPayload(
                LocalDateTime.now(),
                "newemail@example.com",
                "newlogin",
                1L
        );
    }

    @Test
    void testSignUp_Success() {
        // Given
        when(userService.createUser(any(SignUpPayload.class))).thenReturn(testUser);
        when(accessRepository.findByUserName(anyString())).thenReturn(Optional.of(testAccessEntity));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(CustomerDetails.class))).thenReturn("test-token");

        // When
        String result = accessService.signUp(testSignUpPayload);

        // Then
        assertNotNull(result);
        assertEquals("test-token", result);
        verify(userService, times(1)).createUser(testSignUpPayload);
        verify(accessRepository, times(1)).save(any(AccessEntity.class));
        verify(passwordEncoder, times(1)).encode(testSignUpPayload.password());
        verify(jwtService, times(1)).generateToken(any(CustomerDetails.class));
    }

    @Test
    void testCreateAccess_Success() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accessRepository.save(any(AccessEntity.class))).thenReturn(testAccessEntity);

        // When
        accessService.createAccess(testCreateUserEvent);

        // Then
        verify(passwordEncoder, times(1)).encode(testCreateUserEvent.password());
        verify(accessRepository, times(1)).save(argThat(access ->
                access.getEmail().equals(testCreateUserEvent.email()) &&
                access.getUserId().equals(testCreateUserEvent.userId()) &&
                access.getLogin().equals(testCreateUserEvent.login()) &&
                access.getRole().equals("USER")
        ));
    }

    @Test
    void testUpdateAccess_Success() {
        // Given
        AccessEntity existingAccess = AccessEntity.builder()
                .email("old@example.com")
                .userId(1L)
                .login("oldlogin")
                .role("USER")
                .password("encodedPassword")
                .build();

        when(accessRepository.getByUserId(1L)).thenReturn(existingAccess);
        when(accessRepository.findByUserName(anyString())).thenReturn(Optional.of(existingAccess));
        when(jwtService.generateToken(any(CustomerDetails.class))).thenReturn("new-token");

        // When
        String result = accessService.updateAccess(testUpdateUserPayload);

        // Then
        assertNotNull(result);
        assertEquals("new-token", result);
        verify(accessRepository, times(1)).getByUserId(1L);
        verify(accessRepository, times(1)).save(argThat(access ->
                access.getEmail().equals("newemail@example.com") &&
                access.getLogin().equals("newlogin")
        ));
        verify(jwtService, times(1)).generateToken(any(CustomerDetails.class));
    }

    @Test
    void testUpdateAccess_WhenAccessNotFound_ShouldThrowException() {
        // Given
        when(accessRepository.getByUserId(1L)).thenReturn(null);

        // When & Then
        AccessServiceException exception = assertThrows(AccessServiceException.class,
                () -> accessService.updateAccess(testUpdateUserPayload));

        assertEquals("Access not found!", exception.getMessage());
        assertEquals(SystemCode.NOT_FOUND, exception.getSystemCode());
        verify(accessRepository, times(1)).getByUserId(1L);
        verify(accessRepository, never()).save(any(AccessEntity.class));
    }

    @Test
    void testUpdateAccess_WithBlankLogin_ShouldSetLoginToNull() {
        // Given
        AccessEntity existingAccess = AccessEntity.builder()
                .email("old@example.com")
                .userId(1L)
                .login("oldlogin")
                .role("USER")
                .password("encodedPassword")
                .build();

        UpdateUserPayload payloadWithBlankLogin = new UpdateUserPayload(
                LocalDateTime.now(),
                "newemail@example.com",
                "   ", // blank login
                1L
        );

        when(accessRepository.getByUserId(1L)).thenReturn(existingAccess);
        when(accessRepository.findByUserName(anyString())).thenReturn(Optional.of(existingAccess));
        when(jwtService.generateToken(any(CustomerDetails.class))).thenReturn("new-token");

        // When
        accessService.updateAccess(payloadWithBlankLogin);

        // Then
        verify(accessRepository, times(1)).save(argThat(access ->
                access.getEmail().equals("newemail@example.com") &&
                access.getLogin() == null
        ));
    }

    @Test
    void testDeleteAccess_WhenExists_ShouldDelete() {
        // Given
        when(accessRepository.existsById("test@example.com")).thenReturn(true);
        doNothing().when(accessRepository).deleteById("test@example.com");

        // When
        accessService.deleteAccess("test@example.com");

        // Then
        verify(accessRepository, times(1)).existsById("test@example.com");
        verify(accessRepository, times(1)).deleteById("test@example.com");
    }

    @Test
    void testDeleteAccess_WhenNotExists_ShouldNotDelete() {
        // Given
        when(accessRepository.existsById("test@example.com")).thenReturn(false);

        // When
        accessService.deleteAccess("test@example.com");

        // Then
        verify(accessRepository, times(1)).existsById("test@example.com");
        verify(accessRepository, never()).deleteById(anyString());
    }

    @Test
    void testLoadUserByUsername_WithEmail_Success() {
        // Given
        when(accessRepository.findByUserName("test@example.com")).thenReturn(Optional.of(testAccessEntity));

        // When
        UserDetails userDetails = accessService.loadUserByUsername("test@example.com");

        // Then
        assertNotNull(userDetails);
        assertInstanceOf(CustomerDetails.class, userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")));
        verify(accessRepository, times(1)).findByUserName("test@example.com");
    }

    @Test
    void testLoadUserByUsername_WithLogin_Success() {
        // Given
        when(accessRepository.findByUserName("testuser")).thenReturn(Optional.of(testAccessEntity));

        // When
        UserDetails userDetails = accessService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        verify(accessRepository, times(1)).findByUserName("testuser");
    }

    @Test
    void testLoadUserByUsername_WithExternalUser_Success() {
        // Given
        when(accessRepository.findByUserName("testuser")).thenReturn(Optional.empty());
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);
        when(accessRepository.findByUserName("test@example.com")).thenReturn(Optional.of(testAccessEntity));
        when(accessRepository.save(any(AccessEntity.class))).thenReturn(testAccessEntity);

        // When
        UserDetails userDetails = accessService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        verify(userService, times(1)).getUserByLogin("testuser");
        verify(accessRepository, times(1)).save(any(AccessEntity.class));
    }

    @Test
    void testLoadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(accessRepository.findByUserName("nonexistent")).thenReturn(Optional.empty());
        when(userService.getUserByLogin("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> accessService.loadUserByUsername("nonexistent"));

        verify(accessRepository, times(1)).findByUserName("nonexistent");
        verify(userService, times(1)).getUserByLogin("nonexistent");
    }

    @Test
    void testLoadUserByUsername_WhenExternalUserEmailNotFound_ShouldThrowException() {
        // Given
        when(accessRepository.findByUserName("testuser")).thenReturn(Optional.empty());
        when(userService.getUserByLogin("testuser")).thenReturn(testUser);
        when(accessRepository.findByUserName("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class,
                () -> accessService.loadUserByUsername("testuser"));

        verify(userService, times(1)).getUserByLogin("testuser");
        verify(accessRepository, never()).save(any(AccessEntity.class));
    }
}

