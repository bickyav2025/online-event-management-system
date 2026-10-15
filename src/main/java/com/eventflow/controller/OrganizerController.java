package com.eventflow.controller;

import com.eventflow.dto.AnnouncementDto;
import com.eventflow.dto.EventFormDto;
import com.eventflow.dto.TicketTypeDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.EventAnnouncement;
import com.eventflow.entity.Ticket;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.RegistrationStatus;
import com.eventflow.repository.EventAnnouncementRepository;
import com.eventflow.security.CustomUserDetails;
import com.eventflow.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private final EventService eventService;
    private final EventCategoryService categoryService;
    private final TicketService ticketService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final EventAnnouncementRepository announcementRepository;

    public OrganizerController(EventService eventService,
                               EventCategoryService categoryService,
                               TicketService ticketService,
                               RegistrationService registrationService,
                               NotificationService notificationService,
                               EventAnnouncementRepository announcementRepository) {
        this.eventService = eventService;
        this.categoryService = categoryService;
        this.ticketService = ticketService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.announcementRepository = announcementRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User organizer = userDetails.getUser();
        Map<String, Object> stats = eventService.getOrganizerDashboardStats(organizer);
        model.addAllAttributes(stats);
        model.addAttribute("recentEvents", eventService.getOrganizerEvents(organizer, PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent());
        return "organizer/dashboard";
    }

    @GetMapping("/events")
    public String events(@AuthenticationPrincipal CustomUserDetails userDetails,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "10") int size,
                         Model model) {
        User organizer = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> eventsPage = eventService.getOrganizerEvents(organizer, pageable);

        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalElements", eventsPage.getTotalElements());
        return "organizer/events";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("eventDto", new EventFormDto());
        model.addAttribute("categories", categoryService.getActiveCategories());
        return "organizer/event-form";
    }

    @PostMapping("/events/new")
    public String createEvent(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @Valid @ModelAttribute("eventDto") EventFormDto eventDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "organizer/event-form";
        }

        try {
            Event created = eventService.createEvent(userDetails.getUser(), eventDto);
            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully! Now configure tickets for your event.");
            return "redirect:/organizer/tickets?eventId=" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            return "organizer/event-form";
        }
    }

    @GetMapping("/events/edit/{id}")
    public String editEventForm(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        Event event = eventService.findById(id);
        EventFormDto dto = new EventFormDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setShortDescription(event.getShortDescription());
        dto.setFullDescription(event.getFullDescription());
        dto.setCategoryId(event.getCategory().getId());
        dto.setBannerUrl(event.getBannerUrl());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setVenue(event.getVenue());
        dto.setCity(event.getCity());
        dto.setAddress(event.getAddress());
        dto.setOnlineLink(event.getOnlineLink());
        dto.setIsOnline(event.getIsOnline());
        dto.setMaxCapacity(event.getMaxCapacity());
        dto.setRegistrationDeadline(event.getRegistrationDeadline());
        dto.setStatus(event.getStatus());

        model.addAttribute("eventDto", dto);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("isEdit", true);
        return "organizer/event-form";
    }

    @PostMapping("/events/edit/{id}")
    public String updateEvent(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              @Valid @ModelAttribute("eventDto") EventFormDto eventDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        eventDto.setId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("isEdit", true);
            return "organizer/event-form";
        }

        try {
            eventService.updateEvent(userDetails.getUser(), eventDto);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/organizer/events";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("isEdit", true);
            return "organizer/event-form";
        }
    }

    @PostMapping("/events/submit/{id}")
    public String submitForApproval(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            eventService.submitForApproval(userDetails.getUser(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Event submitted for Administrator approval!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/events/publish/{id}")
    public String publishEvent(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            eventService.publishEvent(userDetails.getUser(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Event is now officially PUBLISHED and open for registrations!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(userDetails.getUser(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/events";
    }

    @GetMapping("/tickets")
    public String tickets(@AuthenticationPrincipal CustomUserDetails userDetails,
                          @RequestParam(required = false) Long eventId,
                          Model model) {
        User organizer = userDetails.getUser();
        List<Event> myEvents = eventService.getOrganizerEvents(organizer, Pageable.unpaged()).getContent();

        Event selectedEvent = null;
        if (eventId != null) {
            selectedEvent = myEvents.stream().filter(e -> e.getId().equals(eventId)).findFirst().orElse(null);
        } else if (!myEvents.isEmpty()) {
            selectedEvent = myEvents.get(0);
        }

        model.addAttribute("events", myEvents);
        model.addAttribute("selectedEvent", selectedEvent);
        if (selectedEvent != null) {
            model.addAttribute("ticketTypes", ticketService.getTicketTypesForEvent(selectedEvent));
        }
        model.addAttribute("ticketDto", new TicketTypeDto());
        return "organizer/tickets";
    }

    @PostMapping("/tickets/save")
    public String saveTicket(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute TicketTypeDto dto,
                             RedirectAttributes redirectAttributes) {
        try {
            ticketService.saveTicketType(userDetails.getUser(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket tier saved successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/tickets?eventId=" + dto.getEventId();
    }

    @PostMapping("/tickets/delete/{id}")
    public String deleteTicket(@PathVariable Long id,
                               @RequestParam Long eventId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicketType(userDetails.getUser(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket tier deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/tickets?eventId=" + eventId;
    }

    @GetMapping("/attendees")
    public String attendees(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam(required = false) Long eventId,
                            @RequestParam(required = false) RegistrationStatus status,
                            @RequestParam(required = false) String search,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "15") int size,
                            Model model) {
        User organizer = userDetails.getUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<?> attendeesPage = registrationService.searchOrganizerAttendees(organizer, eventId, status, search, pageable);

        model.addAttribute("attendees", attendeesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", attendeesPage.getTotalPages());
        model.addAttribute("totalElements", attendeesPage.getTotalElements());
        model.addAttribute("events", eventService.getOrganizerEvents(organizer, Pageable.unpaged()).getContent());
        model.addAttribute("selectedEventId", eventId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "organizer/attendees";
    }

    @GetMapping("/attendees/export")
    public void exportAttendeesCsv(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestParam(required = false) Long eventId,
                                   HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendees-" + System.currentTimeMillis() + ".csv\"");

        String csvData = registrationService.exportOrganizerAttendeesCsv(userDetails.getUser(), eventId);
        response.getWriter().write(csvData);
    }

    @GetMapping("/checkin")
    public String checkinPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        return "organizer/checkin";
    }

    @PostMapping("/checkin/verify")
    public String verifyTicketCheckIn(@RequestParam String ticketCode,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            Ticket ticket = ticketService.validateAndCheckIn(ticketCode.trim(), userDetails.getUser());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Entry Approved: " + ticket.getAttendeeName() + " (" + ticket.getTicketTypeName() + ")");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/organizer/checkin";
    }

    @GetMapping("/announcements")
    public String announcements(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User organizer = userDetails.getUser();
        model.addAttribute("events", eventService.getOrganizerEvents(organizer, Pageable.unpaged()).getContent());
        model.addAttribute("announcements", announcementRepository.findByOrganizerOrderByCreatedAtDesc(organizer, PageRequest.of(0, 10)).getContent());
        model.addAttribute("announcementDto", new AnnouncementDto());
        return "organizer/announcements";
    }

    @PostMapping("/announcements")
    public String broadcastAnnouncement(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @Valid @ModelAttribute("announcementDto") AnnouncementDto dto,
                                        BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("events", eventService.getOrganizerEvents(userDetails.getUser(), Pageable.unpaged()).getContent());
            return "organizer/announcements";
        }

        try {
            Event event = eventService.findById(dto.getEventId());
            EventAnnouncement announcement = new EventAnnouncement();
            announcement.setEvent(event);
            announcement.setOrganizer(userDetails.getUser());
            announcement.setTitle(dto.getTitle());
            announcement.setMessage(dto.getMessage());
            announcementRepository.save(announcement);

            notificationService.broadcastAnnouncement(announcement);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement broadcasted to all registered attendees!");
            return "redirect:/organizer/announcements";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/organizer/announcements";
        }
    }
}
