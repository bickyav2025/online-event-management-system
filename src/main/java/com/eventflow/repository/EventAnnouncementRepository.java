package com.eventflow.repository;

import com.eventflow.entity.Event;
import com.eventflow.entity.EventAnnouncement;
import com.eventflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventAnnouncementRepository extends JpaRepository<EventAnnouncement, Long> {
    Page<EventAnnouncement> findByOrganizerOrderByCreatedAtDesc(User organizer, Pageable pageable);
    List<EventAnnouncement> findByEventOrderByCreatedAtDesc(Event event);
}
