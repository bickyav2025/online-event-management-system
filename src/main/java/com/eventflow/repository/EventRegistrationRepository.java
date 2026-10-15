package com.eventflow.repository;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventRegistration;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByRegistrationCode(String registrationCode);

    Page<EventRegistration> findByAttendee(User attendee, Pageable pageable);

    Page<EventRegistration> findByEvent(Event event, Pageable pageable);

    Page<EventRegistration> findByEventAndStatus(Event event, RegistrationStatus status, Pageable pageable);

    List<EventRegistration> findByEvent(Event event);

    boolean existsByEventAndAttendeeAndStatus(Event event, User attendee, RegistrationStatus status);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.organizer = :organizer " +
           "AND (:eventId IS NULL OR r.event.id = :eventId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:search IS NULL OR LOWER(r.attendee.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.attendee.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.registrationCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<EventRegistration> searchOrganizerAttendees(@Param("organizer") User organizer,
                                                     @Param("eventId") Long eventId,
                                                     @Param("status") RegistrationStatus status,
                                                     @Param("search") String search,
                                                     Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM EventRegistration r WHERE r.status = 'CONFIRMED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(r.totalAmount), 0) FROM EventRegistration r WHERE r.event.organizer = :organizer AND r.status = 'CONFIRMED'")
    BigDecimal calculateOrganizerRevenue(@Param("organizer") User organizer);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM EventRegistration r WHERE r.event.organizer = :organizer AND r.status = 'CONFIRMED'")
    Long calculateOrganizerTicketsSold(@Param("organizer") User organizer);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM EventRegistration r WHERE r.status = 'CONFIRMED'")
    Long calculateTotalTicketsSold();

    @Query("SELECT FUNCTION('DATE', r.createdAt), COUNT(r), COALESCE(SUM(r.totalAmount), 0) " +
           "FROM EventRegistration r WHERE r.status = 'CONFIRMED' " +
           "GROUP BY FUNCTION('DATE', r.createdAt) ORDER BY FUNCTION('DATE', r.createdAt) DESC")
    List<Object[]> findDailyRegistrationTrends();

    @Query("SELECT FUNCTION('DATE', r.createdAt), COUNT(r), COALESCE(SUM(r.totalAmount), 0) " +
           "FROM EventRegistration r WHERE r.event.organizer = :organizer AND r.status = 'CONFIRMED' " +
           "GROUP BY FUNCTION('DATE', r.createdAt) ORDER BY FUNCTION('DATE', r.createdAt) DESC")
    List<Object[]> findOrganizerRegistrationTrends(@Param("organizer") User organizer);
}
