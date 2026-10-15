package com.eventflow.controller;

import com.eventflow.entity.EventRegistration;
import com.eventflow.entity.Ticket;
import com.eventflow.entity.User;
import com.eventflow.exception.UnauthorizedException;
import com.eventflow.security.CustomUserDetails;
import com.eventflow.service.NotificationService;
import com.eventflow.service.RegistrationService;
import com.eventflow.service.TicketService;
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

@Controller
@RequestMapping("/attendee")
public class AttendeeController {

    private final RegistrationService registrationService;
    private final TicketService ticketService;
    private final NotificationService notificationService;
    private final UserService userService;

    public AttendeeController(RegistrationService registrationService,
                              TicketService ticketService,
                              NotificationService notificationService,
                              UserService userService) {
        this.registrationService = registrationService;
        this.ticketService = ticketService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User attendee = userDetails.getUser();
        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<EventRegistration> registrations = registrationService.getAttendeeRegistrations(attendee, pageable);

        model.addAttribute("recentRegistrations", registrations.getContent());
        model.addAttribute("totalRegistrations", registrations.getTotalElements());
        model.addAttribute("recentNotifications", notificationService.getRecentNotifications(attendee));
        model.addAttribute("unreadNotificationsCount", notificationService.getUnreadCount(attendee));
        return "attendee/dashboard";
    }

    @GetMapping("/registrations")
    public String registrations(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        User attendee = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EventRegistration> registrationsPage = registrationService.getAttendeeRegistrations(attendee, pageable);

        model.addAttribute("registrations", registrationsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", registrationsPage.getTotalPages());
        model.addAttribute("totalElements", registrationsPage.getTotalElements());
        return "attendee/registrations";
    }

    @GetMapping("/tickets/{code}")
    public String viewTicket(@PathVariable String code,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {
        Ticket ticket = ticketService.findTicketByCode(code);
        User attendee = userDetails.getUser();

        // Security: only allow the attendee or organizer or admin to view
        if (!ticket.getRegistration().getAttendee().getId().equals(attendee.getId())
                && !ticket.getEvent().getOrganizer().getId().equals(attendee.getId())) {
            throw new UnauthorizedException("You are not authorized to view this ticket.");
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("event", ticket.getEvent());
        return "attendee/ticket-view";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "15") int size,
                                Model model) {
        User attendee = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<?> notifsPage = notificationService.getUserNotifications(attendee, pageable);

        model.addAttribute("notifications", notifsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifsPage.getTotalPages());
        return "attendee/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsRead(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           RedirectAttributes redirectAttributes) {
        notificationService.markAllAsRead(userDetails.getUser());
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/attendee/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAsRead(id, userDetails.getUser());
        return "redirect:/attendee/notifications";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userService.findById(userDetails.getId()));
        return "attendee/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam String fullName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String newPassword,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findById(userDetails.getId());
        userService.updateProfile(user, fullName, phone, city, newPassword);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/attendee/profile";
    }
}
