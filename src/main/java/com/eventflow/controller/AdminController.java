package com.eventflow.controller;

import com.eventflow.dto.EventApprovalDto;
import com.eventflow.dto.UserUpdateDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.Role;
import com.eventflow.entity.enums.UserStatus;
import com.eventflow.security.CustomUserDetails;
import com.eventflow.service.ActivityLogService;
import com.eventflow.service.EventService;
import com.eventflow.service.SystemSettingService;
import com.eventflow.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final UserService userService;
    private final SystemSettingService settingService;
    private final ActivityLogService activityLogService;

    public AdminController(EventService eventService,
                           UserService userService,
                           SystemSettingService settingService,
                           ActivityLogService activityLogService) {
        this.eventService = eventService;
        this.userService = userService;
        this.settingService = settingService;
        this.activityLogService = activityLogService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = eventService.getAdminDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("recentLogs", activityLogService.getLogs(null, PageRequest.of(0, 5)).getContent());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(@RequestParam(required = false) String search,
                        @RequestParam(required = false) Role role,
                        @RequestParam(required = false) UserStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> usersPage = userService.searchUsers(search, role, status, pageable);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalUsers", usersPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roles", Role.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleUserStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "User account status successfully updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User account has been deleted.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute UserUpdateDto dto, RedirectAttributes redirectAttributes) {
        userService.updateUserByAdmin(dto);
        redirectAttributes.addFlashAttribute("successMessage", "User details updated successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/events/pending")
    public String pendingEvents(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> pendingPage = eventService.getPendingEvents(pageable);

        model.addAttribute("events", pendingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pendingPage.getTotalPages());
        model.addAttribute("totalElements", pendingPage.getTotalElements());
        return "admin/pending-events";
    }

    @PostMapping("/events/review")
    public String reviewEvent(@ModelAttribute EventApprovalDto approvalDto,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.reviewEvent(
                    approvalDto.getEventId(),
                    approvalDto.isApproved(),
                    approvalDto.getRejectionReason(),
                    userDetails.getUser()
            );
            if (approvalDto.isApproved()) {
                redirectAttributes.addFlashAttribute("successMessage", "Event approved successfully!");
            } else {
                redirectAttributes.addFlashAttribute("warningMessage", "Event rejected with provided feedback.");
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/events/pending";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("settings", settingService.getAllSettings());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        settingService.updateSettings(allParams);
        redirectAttributes.addFlashAttribute("successMessage", "System settings saved successfully.");
        return "redirect:/admin/settings";
    }

    @GetMapping("/activity")
    public String activity(@RequestParam(required = false) String query,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "15") int size,
                           Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<?> logsPage = activityLogService.getLogs(query, pageable);

        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("query", query);
        return "admin/activity";
    }
}
