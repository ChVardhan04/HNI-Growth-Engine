package com.hnigrowth.outreach;

/** Published whenever an outreach message is successfully dispatched, so
 * downstream concerns (follow-up scheduling, analytics) can react without the
 * MessageService needing a direct dependency on them (avoids a service cycle). */
public record MessageSentEvent(OutreachMessage message) {}
