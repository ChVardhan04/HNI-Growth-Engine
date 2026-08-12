package com.hnigrowth.outreach.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Sends every listed message. PENDING_APPROVAL messages are auto-approved
 * by the requesting RM before sending (since this is an explicit bulk-send
 * action the RM initiated); APPROVED messages are sent as-is. */
public record BulkSendRequest(@NotEmpty List<Long> messageIds) {}
