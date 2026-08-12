package com.hnigrowth.linkedin.dto;

public record LinkedInSendResult(
        boolean success,
        String providerMessageId,
        String status,
        String errorMessage
) {
    public static LinkedInSendResult ok(String providerMessageId, String status) {
        return new LinkedInSendResult(true, providerMessageId, status, null);
    }
    public static LinkedInSendResult failed(String errorMessage) {
        return new LinkedInSendResult(false, null, "FAILED", errorMessage);
    }
}
