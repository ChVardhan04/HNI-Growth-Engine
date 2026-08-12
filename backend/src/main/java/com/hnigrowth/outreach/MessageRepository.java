package com.hnigrowth.outreach;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<OutreachMessage, Long> {
    List<OutreachMessage> findByLeadIdOrderByCreatedAtDesc(Long leadId);
    List<OutreachMessage> findByStatusOrderByCreatedAtDesc(ApprovalStatus status);
    long countByStatus(ApprovalStatus status);
}