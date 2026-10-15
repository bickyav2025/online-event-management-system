package com.eventflow.service;

import com.eventflow.dto.EventFormDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.EventCategory;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.EventStatus;
import com.eventflow.entity.enums.NotificationType;
import com.eventflow.exception.BadRequestException;
import com.eventflow.exception.ResourceNotFoundException;
import com.eventflow.exception.UnauthorizedException;
import com.eventflow.repository.EventCategoryRepository;
import com.eventflow.repository.EventRegistrationRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.UserRepository;
import com.eventflow.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public EventService(EventRepository eventRepository,
                        EventCategoryRepository categoryRepository,
                        UserRepository userRepository,
                        EventRegistrationRepository registrationRepository,
                        NotificationService notificationService,
                        ActivityLogService activityLogService) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public Page<Event> searchPublicEvents(String keyword, String categorySlug, String city,
                                          LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        EventCategory category = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            category = categoryRepository.findBySlug(categorySlug).orElse(null);
        }
        if (keyword != null && keyword.isBlank()) keyword = null;
        if (city != null && city.isBlank()) city = null;

        return eventRepository.searchPublicEvents(keyword, category, city, startDate, endDate, pageable);
    }

    public List<Event> getFeaturedEvents() {
        return eventRepository.findTop6ByStatusOrderByStartTimeAsc(EventStatus.PUBLISHED);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with ID: " + id));
    }

    public Event findBySlug(String slug) {
        return eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with slug: " + slug));
    }

    public Page<Event> getOrganizerEvents(User organizer, Pageable pageable) {
        return eventRepository.findByOrganizer(organizer, pageable);
    }

    public Page<Event> getPendingEvents(Pageable pageable) {
        return eventRepository.findByStatus(EventStatus.PENDING_APPROVAL, pageable);
    }

    @Transactional
    public Event createEvent(User organizer, EventFormDto dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BadRequestException("Event end time cannot be before start time.");
        }
        if (dto.getRegistrationDeadline().isAfter(dto.getStartTime())) {
            throw new BadRequestException("Registration deadline must be before or at event start time.");
        }

        EventCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid event category."));

        String baseSlug = SlugUtils.toSlug(dto.getTitle());
        String slug = baseSlug + "-" + System.currentTimeMillis() % 100000;

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setSlug(slug);
        event.setShortDescription(dto.getShortDescription());
        event.setFullDescription(dto.getFullDescription());
        event.setCategory(category);
        event.setOrganizer(organizer);
        event.setBannerUrl(dto.getBannerUrl() != null && !dto.getBannerUrl().isBlank()
                ? dto.getBannerUrl()
                : "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1200&auto=format&fit=crop&q=80");
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setVenue(dto.getVenue());
        event.setCity(dto.getCity());
        event.setAddress(dto.getAddress());
        event.setOnlineLink(dto.getOnlineLink());
        event.setIsOnline(dto.getIsOnline() != null && dto.getIsOnline());
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setRegistrationDeadline(dto.getRegistrationDeadline());
        event.setStatus(dto.getStatus() != null ? dto.getStatus() : EventStatus.DRAFT);

        Event saved = eventRepository.save(event);
        activityLogService.log(organizer.getId(), organizer.getEmail(), "EVENT_CREATED", "Created event: " + saved.getTitle());
        return saved;
    }

    @Transactional
    public Event updateEvent(User organizer, EventFormDto dto) {
        Event event = findById(dto.getId());

        // Security check: organizer can only manage their own events
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to edit this event.");
        }

        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BadRequestException("Event end time cannot be before start time.");
        }

        EventCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Invalid event category."));

        event.setTitle(dto.getTitle());
        event.setShortDescription(dto.getShortDescription());
        event.setFullDescription(dto.getFullDescription());
        event.setCategory(category);
        if (dto.getBannerUrl() != null && !dto.getBannerUrl().isBlank()) {
            event.setBannerUrl(dto.getBannerUrl());
        }
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setVenue(dto.getVenue());
        event.setCity(dto.getCity());
        event.setAddress(dto.getAddress());
        event.setOnlineLink(dto.getOnlineLink());
        event.setIsOnline(dto.getIsOnline() != null && dto.getIsOnline());
        event.setMaxCapacity(dto.getMaxCapacity());
        event.setRegistrationDeadline(dto.getRegistrationDeadline());

        // Status update
        if (dto.getStatus() != null) {
            event.setStatus(dto.getStatus());
        }

        Event updated = eventRepository.save(event);
        activityLogService.log(organizer.getId(), organizer.getEmail(), "EVENT_UPDATED", "Updated event: " + updated.getTitle());
        return updated;
    }

    @Transactional
    public void submitForApproval(User organizer, Long eventId) {
        Event event = findById(eventId);
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to modify this event.");
        }
        event.setStatus(EventStatus.PENDING_APPROVAL);
        eventRepository.save(event);

        activityLogService.log(organizer.getId(), organizer.getEmail(), "EVENT_SUBMITTED_FOR_APPROVAL", "Event: " + event.getTitle());
    }

    @Transactional
    public void publishEvent(User organizer, Long eventId) {
        Event event = findById(eventId);
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to publish this event.");
        }
        if (event.getStatus() != EventStatus.APPROVED && event.getStatus() != EventStatus.DRAFT) {
            throw new BadRequestException("Only approved events can be published.");
        }
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);

        activityLogService.log(organizer.getId(), organizer.getEmail(), "EVENT_PUBLISHED", "Published event: " + event.getTitle());
    }

    @Transactional
    public void reviewEvent(Long eventId, boolean approved, String rejectionReason, User admin) {
        Event event = findById(eventId);
        if (approved) {
            event.setStatus(EventStatus.APPROVED);
            event.setRejectionReason(null);
            notificationService.createNotification(
                    event.getOrganizer(),
                    "Event Approved!",
                    "Your event '" + event.getTitle() + "' has been approved by the platform admin and can now be published.",
                    NotificationType.APPROVAL,
                    "/organizer/events"
            );
        } else {
            if (rejectionReason == null || rejectionReason.isBlank()) {
                throw new BadRequestException("A rejection reason is required.");
            }
            event.setStatus(EventStatus.REJECTED);
            event.setRejectionReason(rejectionReason);
            notificationService.createNotification(
                    event.getOrganizer(),
                    "Event Needs Revisions",
                    "Your event '" + event.getTitle() + "' was not approved. Reason: " + rejectionReason,
                    NotificationType.APPROVAL,
                    "/organizer/events"
            );
        }
        eventRepository.save(event);
        activityLogService.log(admin.getId(), admin.getEmail(), approved ? "EVENT_APPROVED" : "EVENT_REJECTED",
                "Event: " + event.getTitle() + (approved ? "" : " (Reason: " + rejectionReason + ")"));
    }

    @Transactional
    public void deleteEvent(User organizer, Long eventId) {
        Event event = findById(eventId);
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this event.");
        }
        eventRepository.delete(event);
        activityLogService.log(organizer.getId(), organizer.getEmail(), "EVENT_DELETED", "Deleted event: " + event.getTitle());
    }

    public Map<String, Object> getAdminDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalEvents", eventRepository.count());
        stats.put("pendingApprovals", eventRepository.countByStatus(EventStatus.PENDING_APPROVAL));
        stats.put("totalRegistrations", registrationRepository.count());
        stats.put("totalRevenue", registrationRepository.calculateTotalRevenue());
        stats.put("totalTicketsSold", registrationRepository.calculateTotalTicketsSold());
        stats.put("eventsByCategory", eventRepository.countEventsByCategory());
        stats.put("registrationTrends", registrationRepository.findDailyRegistrationTrends());
        return stats;
    }

    public Map<String, Object> getOrganizerDashboardStats(User organizer) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.countByOrganizer(organizer));
        stats.put("pendingEvents", eventRepository.countByOrganizerAndStatus(organizer, EventStatus.PENDING_APPROVAL));
        stats.put("publishedEvents", eventRepository.countByOrganizerAndStatus(organizer, EventStatus.PUBLISHED));
        stats.put("totalRevenue", registrationRepository.calculateOrganizerRevenue(organizer));
        stats.put("ticketsSold", registrationRepository.calculateOrganizerTicketsSold(organizer));
        stats.put("registrationTrends", registrationRepository.findOrganizerRegistrationTrends(organizer));
        return stats;
    }
}
