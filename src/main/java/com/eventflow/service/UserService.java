package com.eventflow.service;

import com.eventflow.dto.UserRegistrationDto;
import com.eventflow.dto.UserUpdateDto;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.Role;
import com.eventflow.entity.enums.UserStatus;
import com.eventflow.exception.BadRequestException;
import com.eventflow.exception.ResourceNotFoundException;
import com.eventflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("An account with this email address already exists.");
        }
        if (dto.getPassword() != null && !dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        // Prevent unauthorized privilege escalation: only allow ATTENDEE or ORGANIZER registration publicly
        Role assignedRole = dto.getRole() == Role.ORGANIZER ? Role.ORGANIZER : Role.ATTENDEE;

        User user = new User(
                dto.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getFullName().trim(),
                dto.getPhone(),
                dto.getCity(),
                assignedRole
        );
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        activityLogService.log(saved.getId(), saved.getEmail(), "USER_REGISTERED", "Registered with role: " + assignedRole);
        return saved;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public Page<User> searchUsers(String query, Role role, UserStatus status, Pageable pageable) {
        return userRepository.searchUsers(query, role, status, pageable);
    }

    @Transactional
    public User updateUserByAdmin(UserUpdateDto dto) {
        User user = findById(dto.getId());

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already taken by another account.");
        }

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPhone(dto.getPhone());
        user.setCity(dto.getCity());
        if (dto.getRole() != null) user.setRole(dto.getRole());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        activityLogService.log(user.getId(), user.getEmail(), "ADMIN_USER_UPDATED", "Updated user details: " + user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = findById(userId);
        UserStatus newStatus = user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE;
        user.setStatus(newStatus);
        userRepository.save(user);
        activityLogService.log(user.getId(), user.getEmail(), "USER_STATUS_TOGGLED", "Status set to: " + newStatus);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
        activityLogService.log(userId, user.getEmail(), "USER_DELETED", "Deleted user: " + user.getEmail());
    }

    @Transactional
    public void updateProfile(User user, String fullName, String phone, String city, String newPassword) {
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setCity(city);
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        userRepository.save(user);
        activityLogService.log(user.getId(), user.getEmail(), "PROFILE_UPDATED", "Updated personal profile");
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        activityLogService.log(user.getId(), user.getEmail(), "PASSWORD_RESET", "Password reset successfully");
    }

    public long countTotalUsers() {
        return userRepository.count();
    }
}
