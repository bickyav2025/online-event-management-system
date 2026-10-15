package com.eventflow.service;

import com.eventflow.entity.ActivityLog;
import com.eventflow.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId, String actorEmail, String action, String details) {
        ActivityLog log = new ActivityLog(actorId, actorEmail, action, details, "127.0.0.1");
        activityLogRepository.save(log);
    }

    public Page<ActivityLog> getLogs(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return activityLogRepository.searchActivityLogs(query, pageable);
        }
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
