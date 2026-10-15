package com.eventflow.service;

import com.eventflow.dto.UserRegistrationDto;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.Role;
import com.eventflow.entity.enums.UserStatus;
import com.eventflow.exception.BadRequestException;
import com.eventflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ActivityLogService activityLogService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, activityLogService);
    }

    @Test
    void registerUser_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName("John Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("Secret123");
        dto.setConfirmPassword("Secret123");
        dto.setRole(Role.ATTENDEE);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(dto);

        assertNotNull(registered);
        assertEquals("john@example.com", registered.getEmail());
        assertEquals("John Doe", registered.getFullName());
        assertEquals(Role.ATTENDEE, registered.getRole());
        assertEquals(UserStatus.ACTIVE, registered.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsBadRequest() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void toggleUserStatus_SwitchesActiveToInactive() {
        User user = new User("test@example.com", "pass", "Test User", null, null, Role.ATTENDEE);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.toggleUserStatus(1L);

        assertEquals(UserStatus.INACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }
}
