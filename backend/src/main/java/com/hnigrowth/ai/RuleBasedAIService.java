package com.hnigrowth.ai;

import com.hnigrowth.ai.llm.LLMProvider;
import com.hnigrowth.ai.message.LLMMessageGenerator;
import com.hnigrowth.ai.message.MessageGenerator;
import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.GeneratedMessage;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.ai.model.ScoringResult;
import com.hnigrowth.ai.recommendation.RecommendationEngine;
import com.hnigrowth.ai.scoring.IcpScoringEngine;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Default AIService implementation that orchestrates the modular engines.
 * Each engine is independently swappable; this class simply composes them.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleBasedAIService implements AIService {

    private final IcpScoringEngine scoringEngine;
    private final MessageGenerator messageGenerator;
    private final LLMMessageGenerator llmMessageGenerator;
    private final RecommendationEngine recommendationEngine;
    private final LLMProvider llmProvider;

    @Override
    public ScoringResult scoreLead(Lead lead) {
        return scoringEngine.score(lead);
    }

    @Override
    public GeneratedMessage generateMessage(Lead lead, Channel channel) {
        return generateMessage(lead, channel, MessageStage.FIRST_MESSAGE, MessageLength.LONG);
    }

    @Override
    public GeneratedMessage generateMessage(Lead lead, Channel channel, MessageStage stage, MessageLength length) {
        // Prefer the real LLM (industry-aware, instruction-driven) whenever one is
        // configured. "rule-based" is the explicit no-LLM setting; anything else
        // (openai/claude/gemini) is treated as a live provider. Never let an LLM
        // failure break message generation -- fall back to the deterministic
        // template generator so approvals/outreach keep working either way.
        if (!"rule-based".equalsIgnoreCase(llmProvider.name())) {
            try {
                return llmMessageGenerator.generate(lead, channel, stage, length);
            } catch (Exception e) {
                log.warn("[ai] LLM message generation failed ({}), falling back to rule-based: {}",
                        llmProvider.name(), e.getMessage());
            }
        }
        return messageGenerator.generate(lead, channel, stage, length);
    }

    @Override
    public Recommendation recommend(Lead lead, int intentScore, IntentBand band) {
        return recommendationEngine.recommend(lead, intentScore, band);
    }

    @Override
    public String activeProvider() {
        return llmProvider.name();
    }
}
