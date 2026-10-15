package com.eventflow.service;

import com.eventflow.dto.EventFormDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.EventCategory;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.EventStatus;
import com.eventflow.entity.enums.Role;
import com.eventflow.exception.UnauthorizedException;
import com.eventflow.repository.EventCategoryRepository;
import com.eventflow.repository.EventRegistrationRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventCategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRegistrationRepository registrationRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ActivityLogService activityLogService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(
                eventRepository,
                categoryRepository,
                userRepository,
                registrationRepository,
                notificationService,
                activityLogService
        );
    }

    @Test
    void createEvent_Success() {
        User organizer = new User("org@example.com", "pass", "Org User", null, null, Role.ORGANIZER);
        organizer.setId(10L);

        EventCategory category = new EventCategory("Technology", "technology", "Desc", "bi-laptop");
        category.setId(1L);

        EventFormDto dto = new EventFormDto();
        dto.setTitle("Spring Boot Summit");
        dto.setCategoryId(1L);
        dto.setVenue("Online");
        dto.setCity("San Francisco");
        dto.setMaxCapacity(200);
        dto.setStartTime(LocalDateTime.now().plusDays(10));
        dto.setEndTime(LocalDateTime.now().plusDays(11));
        dto.setRegistrationDeadline(LocalDateTime.now().plusDays(9));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event created = eventService.createEvent(organizer, dto);

        assertNotNull(created);
        assertEquals("Spring Boot Summit", created.getTitle());
        assertEquals(EventStatus.DRAFT, created.getStatus());
        assertEquals(organizer, created.getOrganizer());
    }

    @Test
    void deleteEvent_OtherOrganizer_ThrowsUnauthorized() {
        User owner = new User("owner@example.com", "pass", "Owner", null, null, Role.ORGANIZER);
        owner.setId(10L);

        User attacker = new User("attacker@example.com", "pass", "Attacker", null, null, Role.ORGANIZER);
        attacker.setId(99L);

        Event event = new Event();
        event.setId(5L);
        event.setOrganizer(owner);

        when(eventRepository.findById(5L)).thenReturn(Optional.of(event));

        assertThrows(UnauthorizedException.class, () -> eventService.deleteEvent(attacker, 5L));
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void reviewEvent_Approved_UpdatesStatusAndNotifies() {
        User organizer = new User("org@example.com", "pass", "Org User", null, null, Role.ORGANIZER);
        organizer.setId(10L);

        User admin = new User("admin@example.com", "pass", "Admin User", null, null, Role.ADMIN);
        admin.setId(1L);

        Event event = new Event();
        event.setId(8L);
        event.setTitle("Tech Expo");
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.PENDING_APPROVAL);

        when(eventRepository.findById(8L)).thenReturn(Optional.of(event));

        eventService.reviewEvent(8L, true, null, admin);

        assertEquals(EventStatus.APPROVED, event.getStatus());
        verify(notificationService).createNotification(eq(organizer), anyString(), anyString(), any(), anyString());
        verify(eventRepository).save(event);
    }
}
