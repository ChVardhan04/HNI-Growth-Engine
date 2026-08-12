package com.hnigrowth.provider;

/** Abstraction for delivering RM-facing notifications (in-app, email, webhook, ...). */
public interface NotificationProvider {
    void notify(Long rmId, String title, String message, String link);
}
