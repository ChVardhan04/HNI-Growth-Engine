package com.hnigrowth.ai.model;

/** An explainable next-best-action recommendation. */
public record Recommendation(
        RecommendationType action,
        String reasoning,
        int confidence
) {}
