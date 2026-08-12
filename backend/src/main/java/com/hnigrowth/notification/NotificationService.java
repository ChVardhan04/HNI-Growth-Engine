package com.hnigrowth.notification;

import com.hnigrowth.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * In-app notification delivery. Implements NotificationProvider so it can be
 * swapped/augmented with email or webhook delivery without touching callers
 * (e.g. hot-lead detection in IntentService).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements NotificationProvider {

    private final NotificationRepository repository;

    @Override
    @Transactional
    public void notify(Long rmId, String title, String message, String link) {
        notify(rmId, title, message, link, NotificationType.INFO);
    }

    @Transactional
    public Notification notify(Long rmId, String title, String message, String link, NotificationType type) {
        Notification n = repository.save(Notification.builder()
                .rmId(rmId).title(title).message(message).link(link).type(type).build());
        log.info("[notification] {} -> RM {}: {}", type, rmId, title);
        return n;
    }

    @Transactional(readOnly = true)
    public List<Notification> forRm(Long rmId) { return repository.findByRmIdOrderByCreatedAtDesc(rmId); }

    @Transactional(readOnly = true)
    public List<Notification> unreadForRm(Long rmId) { return repository.findByRmIdAndReadFalseOrderByCreatedAtDesc(rmId); }

    @Transactional
    public void markRead(Long id) {
        repository.findById(id).ifPresent(n -> { n.setRead(true); repository.save(n); });
    }
}
