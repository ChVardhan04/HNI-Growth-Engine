package com.hnigrowth.ai.recommendation;

import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.Lead;

/** Recommends the next best action for a lead, with reasoning. */
public interface RecommendationEngine {
    Recommendation recommend(Lead lead, int intentScore, IntentBand band);
}
