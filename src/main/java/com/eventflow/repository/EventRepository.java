package com.eventflow.repository;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventCategory;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    Page<Event> findByOrganizer(User organizer, Pageable pageable);

    Page<Event> findByOrganizerAndStatus(User organizer, EventStatus status, Pageable pageable);

    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    List<Event> findTop6ByStatusOrderByStartTimeAsc(EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.status = 'PUBLISHED' AND " +
           "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.city) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:city IS NULL OR LOWER(e.city) = LOWER(:city)) AND " +
           "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR e.startTime <= :endDate)")
    Page<Event> searchPublicEvents(@Param("keyword") String keyword,
                                   @Param("category") EventCategory category,
                                   @Param("city") String city,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    long countByStatus(EventStatus status);

    long countByOrganizer(User organizer);

    long countByOrganizerAndStatus(User organizer, EventStatus status);

    @Query("SELECT e.category.name, COUNT(e) FROM Event e GROUP BY e.category.name")
    List<Object[]> countEventsByCategory();

    @Query("SELECT FUNCTION('DATE', e.createdAt), COUNT(e) FROM Event e GROUP BY FUNCTION('DATE', e.createdAt) ORDER BY FUNCTION('DATE', e.createdAt) DESC")
    List<Object[]> countEventCreationTrend();
}
