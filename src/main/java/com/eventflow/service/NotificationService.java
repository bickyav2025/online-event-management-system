package com.eventflow.service;

import com.eventflow.entity.EventAnnouncement;
import com.eventflow.entity.EventRegistration;
import com.eventflow.entity.Notification;
import com.eventflow.entity.User;
import com.eventflow.entity.enums.NotificationType;
import com.eventflow.repository.EventRegistrationRepository;
import com.eventflow.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EventRegistrationRepository registrationRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               EventRegistrationRepository registrationRepository) {
        this.notificationRepository = notificationRepository;
        this.registrationRepository = registrationRepository;
    }

    @Transactional
    public Notification createNotification(User user, String title, String message, NotificationType type, String targetUrl) {
        Notification notification = new Notification(user, title, message, type, targetUrl);
        return notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadForUser(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, User user) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void broadcastAnnouncement(EventAnnouncement announcement) {
        List<EventRegistration> registrations = registrationRepository.findByEvent(announcement.getEvent());
        Set<Long> notifiedUserIds = new HashSet<>();

        for (EventRegistration reg : registrations) {
            User attendee = reg.getAttendee();
            if (attendee != null && notifiedUserIds.add(attendee.getId())) {
                createNotification(
                        attendee,
                        "Announcement: " + announcement.getTitle(),
                        announcement.getMessage(),
                        NotificationType.ANNOUNCEMENT,
                        "/attendee/registrations"
                );
            }
        }
    }
}
