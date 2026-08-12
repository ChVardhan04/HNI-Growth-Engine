package com.hnigrowth.followup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByStatusAndScheduledAtBefore(FollowUpStatus status, Instant instant);
    List<FollowUp> findByLeadIdAndStatus(Long leadId, FollowUpStatus status);
}
