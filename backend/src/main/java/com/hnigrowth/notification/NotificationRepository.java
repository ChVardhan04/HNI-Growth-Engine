package com.hnigrowth.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRmIdOrderByCreatedAtDesc(Long rmId);
    List<Notification> findByRmIdAndReadFalseOrderByCreatedAtDesc(Long rmId);
    long countByRmIdAndReadFalse(Long rmId);
}
