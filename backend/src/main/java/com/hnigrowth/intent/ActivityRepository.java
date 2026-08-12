package com.hnigrowth.intent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByLeadIdOrderByCreatedAtDesc(Long leadId);
    long countByCreatedAtAfter(java.time.Instant instant);
}
