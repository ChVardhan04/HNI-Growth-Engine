package com.hnigrowth.ai.message;

import com.hnigrowth.ai.llm.LLMProvider;
import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.GeneratedMessage;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.ai.prompt.PromptBuilder;
import com.hnigrowth.lead.Lead;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Real LLM-backed message generation, using whichever LLMProvider is active
 * (see app.ai.provider). Instructions -- including per-industry guidance --
 * come from PromptBuilder, which pulls RM-editable text from Settings
 * (SettingKey.MESSAGE_GENERATION_INSTRUCTIONS) so the sales team can steer
 * tone/industry framing without a code change or redeploy.
 *
 * This is deliberately a separate class from RuleBasedMessageGenerator (the
 * fallback) rather than a branch inside it, so a live LLM outage/error can
 * never take down message generation -- see RuleBasedAIService, which tries
 * this first and falls back to the rule-based generator on any failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LLMMessageGenerator {

    private final LLMProvider llmProvider;
    private final PromptBuilder promptBuilder;

    public GeneratedMessage generate(Lead lead, Channel channel, MessageStage stage, MessageLength length) {
        String prompt = promptBuilder.messagePrompt(lead, channel, stage, length);
        String raw = llmProvider.complete(prompt);
        String cleaned = clean(raw);
        log.info("[ai] LLM ({}) generated {}/{} message for lead {}", llmProvider.name(), channel, stage, lead.getId());
        return new GeneratedMessage(channel, null, cleaned);
    }

    /** Strips surrounding quotes/markdown fencing/label prefixes the model
     * sometimes adds despite being told not to -- keeps the RM-facing draft clean. */
    private String clean(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.startsWith("```")) {
            s = s.replaceFirst("^```[a-zA-Z]*\\n?", "").replaceFirst("```\\s*$", "").trim();
        }
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        s = s.replaceFirst("(?i)^(message|subject|body)\\s*:\\s*", "");
        return s;
    }
}
