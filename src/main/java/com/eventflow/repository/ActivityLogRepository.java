package com.eventflow.repository;

import com.eventflow.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM ActivityLog a WHERE " +
           "(:query IS NULL OR LOWER(a.action) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.actorEmail) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.details) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> searchActivityLogs(@Param("query") String query, Pageable pageable);
}
