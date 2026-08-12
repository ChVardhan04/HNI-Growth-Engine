package com.hnigrowth.ai.prompt;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.settings.SettingKey;
import com.hnigrowth.settings.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds structured prompts from lead context. Rule-based engines don't
 * strictly need these, but keeping prompt construction here means an
 * LLM-backed engine can reuse the exact same context assembly.
 */
@Component
@RequiredArgsConstructor
public class PromptBuilder {

    private final SettingsService settingsService;

    /** Default per-industry talking points used when no custom instructions are
     * configured in Settings. RM-editable overrides always take precedence. */
    private static final String DEFAULT_MESSAGE_INSTRUCTIONS = """
            General tone: warm, consultative, never pushy. Keep it concise -- LinkedIn
            messages are skimmed, not read closely. Never use generic filler like
            "I hope this finds you well" or "I wanted to reach out".

            Tailor references by industry when known:
            - BFSI / Finance: reference regulatory trust, capital preservation, and
              long-term wealth stewardship. Avoid generic "investment opportunity" language.
            - Technology: reference growth stage, equity/liquidity events, and scaling wealth
              alongside a fast-moving career.
            - Manufacturing / Industrials: reference succession planning, family wealth
              continuity, and capital-intensive business cycles.
            - Healthcare: reference practice ownership, timing around major career
              milestones, and long-horizon planning.
            - Real Estate / Construction: reference portfolio diversification beyond
              real assets and liquidity planning.
            If the industry doesn't match any of the above, keep the message generic
            but professionally warm rather than guessing at industry-specific details.
            """;

    public String scoringPrompt(Lead lead) {
        return """
                You are an ICP scoring assistant for a wealth-management firm targeting HNIs.
                Score this prospect 0-100 and assign a tier (A+, A, B, C, D). Explain the reason.
                Prospect:
                %s
                """.formatted(leadContext(lead));
    }

    /** Stage- and length-aware message prompt, prefixed with RM-configured
     * instructions (Settings -> MESSAGE_GENERATION_INSTRUCTIONS) so the sales
     * lead can steer tone/industry framing without a code change. */
    public String messagePrompt(Lead lead, Channel channel, MessageStage stage, MessageLength length) {
        String instructions = settingsService.get(SettingKey.MESSAGE_GENERATION_INSTRUCTIONS)
                .filter(s -> !s.isBlank())
                .orElse(DEFAULT_MESSAGE_INSTRUCTIONS);

        String stageGuidance = switch (stage) {
            case CONNECTION_REQUEST -> "This is a LinkedIn CONNECTION REQUEST note (max ~300 characters). "
                    + "No greeting fluff -- get to a genuine, specific reason to connect.";
            case FIRST_MESSAGE -> "This is the FIRST MESSAGE after the prospect accepted the connection. "
                    + "Reference something specific about their role/company, then a soft ask for a short call.";
            case FOLLOW_UP_1 -> "This is FOLLOW-UP #1 (a few days after the first message, no reply yet). "
                    + "Brief, low-pressure nudge -- do not repeat the first message verbatim.";
            case FOLLOW_UP_2 -> "This is FOLLOW-UP #2. Slightly more direct, still respectful. "
                    + "Offer a specific, low-commitment next step (e.g. a 10-minute call).";
            case FOLLOW_UP_3 -> "This is FOLLOW-UP #3, the final touch in this sequence. "
                    + "Warm close -- leave the door open without being pushy, since no further automated "
                    + "follow-up will be sent after this.";
        };

        String lengthGuidance = length == MessageLength.SHORT
                ? "Keep it SHORT: 1-2 sentences, under 400 characters total."
                : "LONG version: 3-5 sentences, but still tight -- no rambling.";

        return """
                %s

                %s
                %s

                Write a personalized %s outreach message for this HNI prospect, on behalf of
                a relationship manager at a wealth-management firm. Output ONLY the message
                text -- no subject line, no quotation marks, no explanation, no markdown.

                Prospect:
                %s
                """.formatted(instructions, stageGuidance, lengthGuidance, channel, leadContext(lead));
    }

    public String recommendationPrompt(Lead lead, int intentScore) {
        return """
                Given this prospect and an intent score of %d/100, recommend the single
                next best action and explain why.
                Prospect:
                %s
                """.formatted(intentScore, leadContext(lead));
    }

    private String leadContext(Lead lead) {
        return """
                - Name: %s
                - Designation: %s
                - Company: %s
                - Industry: %s
                - Location: %s
                - Years experience: %s
                - Source: %s
                - LinkedIn: %s
                - Email: %s
                - Bio/About (from LinkedIn, if available): %s
                """.formatted(
                nz(lead.getName()), nz(lead.getDesignation()), nz(lead.getCompany()),
                nz(lead.getIndustry()), nz(lead.getLocation()),
                lead.getYearsExperience() == null ? "unknown" : lead.getYearsExperience().toString(),
                lead.getSource(), lead.getLinkedinUrl() == null ? "absent" : "present",
                nz(lead.getEmail()), nz(lead.getNotes()));
    }

    private String nz(String s) { return s == null ? "unknown" : s; }
}
