package com.eventflow.service;

import com.eventflow.dto.BookingRequestDto;
import com.eventflow.entity.*;
import com.eventflow.entity.enums.EventStatus;
import com.eventflow.entity.enums.RegistrationStatus;
import com.eventflow.entity.enums.Role;
import com.eventflow.entity.enums.TicketTier;
import com.eventflow.exception.BadRequestException;
import com.eventflow.repository.EventRegistrationRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.TicketRepository;
import com.eventflow.repository.TicketTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private EventRegistrationRepository registrationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private ActivityLogService activityLogService;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                registrationRepository,
                eventRepository,
                ticketTypeRepository,
                ticketRepository,
                paymentService,
                notificationService,
                activityLogService
        );
    }

    @Test
    void processBooking_Success() {
        User attendee = new User("attendee@example.com", "pass", "Alex", null, null, Role.ATTENDEE);
        attendee.setId(5L);

        Event event = new Event();
        event.setId(1L);
        event.setTitle("AI Summit");
        event.setStatus(EventStatus.PUBLISHED);
        event.setRegistrationDeadline(LocalDateTime.now().plusDays(5));

        TicketType tier = new TicketType();
        tier.setId(2L);
        tier.setEvent(event);
        tier.setName("General Pass");
        tier.setType(TicketTier.GENERAL);
        tier.setPrice(new BigDecimal("50.00"));
        tier.setTotalQuantity(100);
        tier.setAvailableQuantity(50);
        tier.setMaxPerAttendee(5);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setEventId(1L);
        dto.setTicketTypeId(2L);
        dto.setQuantity(2);
        dto.setAttendeeName("Alex M");
        dto.setAttendeeEmail("attendee@example.com");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByEventAndAttendeeAndStatus(event, attendee, RegistrationStatus.CONFIRMED)).thenReturn(false);
        when(ticketTypeRepository.findByIdWithLock(2L)).thenReturn(Optional.of(tier));
        when(registrationRepository.save(any(EventRegistration.class))).thenAnswer(i -> i.getArgument(0));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        EventRegistration reg = registrationService.processBooking(attendee, dto);

        assertNotNull(reg);
        assertEquals(48, tier.getAvailableQuantity()); // 50 - 2
        assertEquals(new BigDecimal("100.00"), reg.getTotalAmount()); // 50 * 2
        verify(ticketRepository, times(2)).save(any(Ticket.class));
        verify(paymentService).processPayment(any(EventRegistration.class), any(), any());
    }

    @Test
    void processBooking_DuplicateRegistration_ThrowsBadRequest() {
        User attendee = new User("attendee@example.com", "pass", "Alex", null, null, Role.ATTENDEE);
        attendee.setId(5L);

        Event event = new Event();
        event.setId(1L);
        event.setStatus(EventStatus.PUBLISHED);
        event.setRegistrationDeadline(LocalDateTime.now().plusDays(5));

        BookingRequestDto dto = new BookingRequestDto();
        dto.setEventId(1L);
        dto.setTicketTypeId(2L);
        dto.setQuantity(1);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByEventAndAttendeeAndStatus(event, attendee, RegistrationStatus.CONFIRMED)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> registrationService.processBooking(attendee, dto));
        verify(ticketTypeRepository, never()).findByIdWithLock(anyLong());
    }

    @Test
    void processBooking_SoldOut_ThrowsBadRequest() {
        User attendee = new User("attendee@example.com", "pass", "Alex", null, null, Role.ATTENDEE);
        attendee.setId(5L);

        Event event = new Event();
        event.setId(1L);
        event.setStatus(EventStatus.PUBLISHED);
        event.setRegistrationDeadline(LocalDateTime.now().plusDays(5));

        TicketType tier = new TicketType();
        tier.setId(2L);
        tier.setEvent(event);
        tier.setAvailableQuantity(1); // Only 1 left
        tier.setMaxPerAttendee(5);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setEventId(1L);
        dto.setTicketTypeId(2L);
        dto.setQuantity(3); // Requested 3

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByEventAndAttendeeAndStatus(event, attendee, RegistrationStatus.CONFIRMED)).thenReturn(false);
        when(ticketTypeRepository.findByIdWithLock(2L)).thenReturn(Optional.of(tier));

        assertThrows(BadRequestException.class, () -> registrationService.processBooking(attendee, dto));
    }
}
