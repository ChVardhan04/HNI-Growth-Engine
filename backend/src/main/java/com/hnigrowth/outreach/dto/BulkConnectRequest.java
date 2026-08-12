package com.hnigrowth.outreach.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Sends a LinkedIn connection request to every listed lead using one shared
 * template. Placeholders supported in messageTemplate: {{firstName}},
 * {{name}}, {{company}}, {{designation}}. If messageTemplate is blank, a
 * sensible default connection note is used.
 */
public record BulkConnectRequest(@NotEmpty List<Long> leadIds, String messageTemplate) {}
