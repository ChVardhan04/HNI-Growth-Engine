package com.hnigrowth.ai;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.GeneratedMessage;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.ai.model.ScoringResult;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.Lead;

/**
 * Facade over the entire AI layer. Business/service classes depend ONLY on this
 * interface, never on concrete engines or providers. This is the seam that lets
 * you replace rule-based logic with OpenAI/Gemini/Claude/Azure OpenAI later
 * without changing any business code.
 */
public interface AIService {

    ScoringResult scoreLead(Lead lead);

    GeneratedMessage generateMessage(Lead lead, Channel channel);

    GeneratedMessage generateMessage(Lead lead, Channel channel, MessageStage stage, MessageLength length);

    Recommendation recommend(Lead lead, int intentScore, IntentBand band);

    /** @return the active provider identifier, for observability/UI display. */
    String activeProvider();
}
