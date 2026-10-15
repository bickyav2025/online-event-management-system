package com.eventflow.service;

import com.eventflow.dto.TicketTypeDto;
import com.eventflow.entity.Event;
import com.eventflow.entity.Ticket;
import com.eventflow.entity.TicketType;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.TicketStatus;
import com.eventflow.exception.BadRequestException;
import com.eventflow.exception.ResourceNotFoundException;
import com.eventflow.exception.UnauthorizedException;
import com.eventflow.repository.EventRepository;
import com.eventflow.repository.TicketRepository;
import com.eventflow.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final ActivityLogService activityLogService;

    public TicketService(TicketTypeRepository ticketTypeRepository,
                         EventRepository eventRepository,
                         TicketRepository ticketRepository,
                         ActivityLogService activityLogService) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.activityLogService = activityLogService;
    }

    public List<TicketType> getTicketTypesForEvent(Event event) {
        return ticketTypeRepository.findByEventOrderByPriceAsc(event);
    }

    public TicketType findById(Long id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket tier not found with id: " + id));
    }

    @Transactional
    public TicketType saveTicketType(User organizer, TicketTypeDto dto) {
        Event event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to modify tickets for this event.");
        }

        TicketType ticketType;
        if (dto.getId() != null) {
            ticketType = findById(dto.getId());
            ticketType.setName(dto.getName());
            ticketType.setType(dto.getType());
            ticketType.setDescription(dto.getDescription());
            ticketType.setPrice(dto.getPrice());
            ticketType.setTotalQuantity(dto.getTotalQuantity());
            ticketType.setMaxPerAttendee(dto.getMaxPerAttendee());
            ticketType.setSalesStart(dto.getSalesStart());
            ticketType.setSalesEnd(dto.getSalesEnd());
        } else {
            ticketType = new TicketType();
            ticketType.setEvent(event);
            ticketType.setName(dto.getName());
            ticketType.setType(dto.getType());
            ticketType.setDescription(dto.getDescription());
            ticketType.setPrice(dto.getPrice());
            ticketType.setTotalQuantity(dto.getTotalQuantity());
            ticketType.setAvailableQuantity(dto.getTotalQuantity());
            ticketType.setMaxPerAttendee(dto.getMaxPerAttendee());
            ticketType.setSalesStart(dto.getSalesStart());
            ticketType.setSalesEnd(dto.getSalesEnd());
        }

        TicketType saved = ticketTypeRepository.save(ticketType);
        activityLogService.log(organizer.getId(), organizer.getEmail(), "TICKET_TIER_CONFIGURED",
                "Configured " + saved.getName() + " for event: " + event.getTitle());
        return saved;
    }

    @Transactional
    public void deleteTicketType(User organizer, Long ticketTypeId) {
        TicketType ticketType = findById(ticketTypeId);
        if (!ticketType.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this ticket tier.");
        }
        if (ticketType.getSoldQuantity() > 0) {
            throw new BadRequestException("Cannot delete ticket tier that already has active registrations.");
        }
        ticketTypeRepository.delete(ticketType);
    }

    @Transactional
    public Ticket validateAndCheckIn(String ticketCode, User organizer) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid ticket code: " + ticketCode));

        if (!ticket.getEvent().getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedException("Unauthorized check-in attempt for another organizer's event.");
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new BadRequestException("Ticket has ALREADY been checked in at " + ticket.getCheckedInAt());
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BadRequestException("Ticket is CANCELLED and invalid for entry.");
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setCheckedInAt(LocalDateTime.now());
        Ticket saved = ticketRepository.save(ticket);

        activityLogService.log(organizer.getId(), organizer.getEmail(), "TICKET_CHECKED_IN",
                "Checked in ticket: " + ticketCode + " for attendee: " + ticket.getAttendeeName());
        return saved;
    }

    public Ticket findTicketByCode(String code) {
        return ticketRepository.findByTicketCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }
}
