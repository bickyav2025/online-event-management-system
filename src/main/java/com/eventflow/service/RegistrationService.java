package com.eventflow.service;

import com.eventflow.dto.BookingRequestDto;
import com.eventflow.entity.*;
import com.eventflow.entity.enums.EventStatus;
import com.eventflow.entity.enums.NotificationType;
import com.eventflow.entity.enums.RegistrationStatus;
import com.eventflow.entity.enums.TicketStatus;
import com.eventflow.exception.BadRequestException;
import com.eventflow.exception.ResourceNotFoundException;
import com.eventflow.exception.UnauthorizedException;
import com.eventflow.repository.EventRegistrationRepository;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.TicketRepository;
import com.eventflow.repository.TicketTypeRepository;
import com.eventflow.util.CsvExporter;
import com.eventflow.util.QrCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationService {

    private final EventRegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public RegistrationService(EventRegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               TicketTypeRepository ticketTypeRepository,
                               TicketRepository ticketRepository,
                               PaymentService paymentService,
                               NotificationService notificationService,
                               ActivityLogService activityLogService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.ticketRepository = ticketRepository;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public EventRegistration processBooking(User attendee, BookingRequestDto dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // 1. Validate Event Status
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Event is currently not open for registration.");
        }

        // 2. Validate Registration Deadline
        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            throw new BadRequestException("Registration for this event has closed.");
        }

        // 3. Prevent duplicate active registrations for the same user on the same event
        if (registrationRepository.existsByEventAndAttendeeAndStatus(event, attendee, RegistrationStatus.CONFIRMED)) {
            throw new BadRequestException("You are already registered for this event. View your ticket in your dashboard.");
        }

        // 4. Pessimistic Write Lock on TicketType to prevent overselling race conditions
        TicketType ticketType = ticketTypeRepository.findByIdWithLock(dto.getTicketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found"));

        if (!ticketType.getEvent().getId().equals(event.getId())) {
            throw new BadRequestException("Selected ticket type does not belong to this event.");
        }

        int requestedQty = dto.getQuantity();
        if (requestedQty <= 0) {
            throw new BadRequestException("Invalid ticket quantity.");
        }

        if (ticketType.getMaxPerAttendee() != null && requestedQty > ticketType.getMaxPerAttendee()) {
            throw new BadRequestException("Cannot purchase more than " + ticketType.getMaxPerAttendee() + " tickets per order.");
        }

        if (ticketType.getAvailableQuantity() < requestedQty) {
            throw new BadRequestException("Sold out or insufficient tickets available. Remaining: " + ticketType.getAvailableQuantity());
        }

        // Decrement ticket inventory safely under lock
        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() - requestedQty);
        ticketTypeRepository.save(ticketType);

        // Calculate amount
        BigDecimal totalAmount = ticketType.getPrice().multiply(BigDecimal.valueOf(requestedQty));

        // Create EventRegistration
        EventRegistration registration = new EventRegistration();
        registration.setRegistrationCode("REG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        registration.setEvent(event);
        registration.setAttendee(attendee);
        registration.setTicketType(ticketType);
        registration.setQuantity(requestedQty);
        registration.setTotalAmount(totalAmount);
        registration.setStatus(RegistrationStatus.CONFIRMED);

        EventRegistration savedRegistration = registrationRepository.save(registration);

        // Generate Digital Tickets with unique QR Codes
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 1; i <= requestedQty; i++) {
            Ticket ticket = new Ticket();
            String ticketCode = "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            ticket.setTicketCode(ticketCode);
            ticket.setRegistration(savedRegistration);
            ticket.setEvent(event);
            ticket.setAttendeeName(dto.getAttendeeName() != null && !dto.getAttendeeName().isBlank() ? dto.getAttendeeName() : attendee.getFullName());
            ticket.setAttendeeEmail(dto.getAttendeeEmail() != null && !dto.getAttendeeEmail().isBlank() ? dto.getAttendeeEmail() : attendee.getEmail());
            ticket.setTicketTypeName(ticketType.getName());
            ticket.setPrice(ticketType.getPrice());
            ticket.setStatus(TicketStatus.VALID);

            // Generate base64 QR Code string containing verification metadata
            String qrPayload = "EVENTFLOW-TICKET:" + ticketCode + "|EVENT:" + event.getId() + "|ATTENDEE:" + ticket.getAttendeeEmail();
            ticket.setQrCodePath(QrCodeGenerator.generateQrCodeBase64(qrPayload, 200, 200));

            tickets.add(ticketRepository.save(ticket));
        }
        savedRegistration.setTickets(tickets);

        // Process Payment (Server-side validation)
        paymentService.processPayment(savedRegistration, dto.getPaymentMethod(), null);

        // Send confirmation notification
        notificationService.createNotification(
                attendee,
                "Booking Confirmed: " + event.getTitle(),
                "Your registration for '" + event.getTitle() + "' is confirmed! Code: " + savedRegistration.getRegistrationCode(),
                NotificationType.REGISTRATION,
                "/attendee/registrations"
        );

        activityLogService.log(attendee.getId(), attendee.getEmail(), "BOOKING_CONFIRMED",
                "Registered " + requestedQty + " ticket(s) for event: " + event.getTitle());

        return savedRegistration;
    }

    public Page<EventRegistration> getAttendeeRegistrations(User attendee, Pageable pageable) {
        return registrationRepository.findByAttendee(attendee, pageable);
    }

    public EventRegistration findByCode(String code) {
        return registrationRepository.findByRegistrationCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found: " + code));
    }

    public Page<EventRegistration> searchOrganizerAttendees(User organizer, Long eventId, RegistrationStatus status, String search, Pageable pageable) {
        if (search != null && search.isBlank()) search = null;
        return registrationRepository.searchOrganizerAttendees(organizer, eventId, status, search, pageable);
    }

    public String exportOrganizerAttendeesCsv(User organizer, Long eventId) {
        List<EventRegistration> list;
        if (eventId != null) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
            if (!event.getOrganizer().getId().equals(organizer.getId())) {
                throw new UnauthorizedException("Unauthorized access to event attendees.");
            }
            list = registrationRepository.findByEvent(event);
        } else {
            list = registrationRepository.searchOrganizerAttendees(organizer, null, null, null, Pageable.unpaged()).getContent();
        }
        return CsvExporter.exportAttendeesToCsv(list);
    }

    public List<Ticket> getTicketsForRegistration(EventRegistration registration) {
        return ticketRepository.findByRegistration(registration);
    }
}
