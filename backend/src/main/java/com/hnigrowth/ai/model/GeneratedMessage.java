package com.hnigrowth.ai.model;

/** A personalized, editable message draft for a given channel. */
public record GeneratedMessage(
        Channel channel,
        String subject,   // null for channels without subjects (e.g. WhatsApp/SMS)
        String body
) {}
