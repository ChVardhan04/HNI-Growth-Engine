package com.hnigrowth.ai.model;

import com.hnigrowth.lead.Tier;

/**
 * Result of ICP scoring. Every field is explainable (see reason/confidence)
 * to satisfy the AI Explanation Engine requirement.
 */
public record ScoringResult(
        int score,                 // 0-100
        Tier tier,
        String reason,             // human-readable explanation
        int confidence,            // 0-100
        RecommendationType nextBestAction
) {}
