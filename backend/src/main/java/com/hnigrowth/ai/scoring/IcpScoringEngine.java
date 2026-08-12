package com.hnigrowth.ai.scoring;

import com.hnigrowth.ai.model.ScoringResult;
import com.hnigrowth.lead.Lead;

/** Computes an explainable ICP score for a lead. */
public interface IcpScoringEngine {
    ScoringResult score(Lead lead);
}
