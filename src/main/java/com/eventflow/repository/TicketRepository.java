package com.eventflow.repository;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventRegistration;
import com.eventflow.entity.Ticket;
import com.eventflow.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByRegistration(EventRegistration registration);

    Page<Ticket> findByEvent(Event event, Pageable pageable);

    long countByEventAndStatus(Event event, TicketStatus status);
}
