package com.hnigrowth.linkedin.dto;

public record LinkedInMessageRequest(
        String recipientProviderProfileId,
        String message,
        LinkedInMessageType type
) {
    public enum LinkedInMessageType { CONNECTION_REQUEST, DIRECT_MESSAGE, FOLLOW_UP }
}
