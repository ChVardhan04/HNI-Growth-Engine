package com.hnigrowth.provider;

/**
 * Generic outcome of a MessagingProvider send attempt, channel-agnostic
 * (LinkedIn/Email/WhatsApp/SMS all return this rather than a LinkedIn-specific
 * type, so providers stay decoupled from each other).
 */
public record MessageSendResult(boolean success, String providerMessageId, String status, String errorMessage) {
    public static MessageSendResult ok(String providerMessageId, String status) {
        return new MessageSendResult(true, providerMessageId, status, null);
    }
    public static MessageSendResult failed(String errorMessage) {
        return new MessageSendResult(false, null, "FAILED", errorMessage);
    }
}
