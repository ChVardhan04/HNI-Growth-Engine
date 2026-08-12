package com.hnigrowth.ai.message;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.GeneratedMessage;
import com.hnigrowth.ai.model.MessageLength;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.lead.Lead;
import org.springframework.stereotype.Component;

/** Template-driven personalization. Output is always editable by the RM. This is the
 * "Rule Based fallback" referenced in the spec -- used whenever the LLM provider
 * is unavailable or unconfigured (see RuleBasedLLMProvider / OpenAiLLMProvider). */
@Component
public class RuleBasedMessageGenerator implements MessageGenerator {

    @Override
    public GeneratedMessage generate(Lead lead, Channel channel, MessageStage stage, MessageLength length) {
        String firstName = firstName(lead.getName());
        String company = lead.getCompany() == null ? "your organization" : lead.getCompany();
        String role = lead.getDesignation() == null ? "your role" : lead.getDesignation();
        boolean isShort = length == MessageLength.SHORT;

        if (channel == Channel.LINKEDIN) {
            return switch (stage) {
                case CONNECTION_REQUEST -> new GeneratedMessage(Channel.LINKEDIN, null,
                        "Hi " + firstName + ", I work with senior leaders like yourself at " + company
                                + " on bespoke wealth strategies. Would love to connect.");
                case FIRST_MESSAGE -> new GeneratedMessage(Channel.LINKEDIN, null, isShort
                        ? "Thanks for connecting, " + firstName + "! Open to a quick chat on wealth strategies tailored to your goals?"
                        : ("Thanks for connecting, " + firstName + "! I admire your journey as " + role + " at " + company
                                + ". We partner with accomplished professionals to build and protect long-term wealth through "
                                + "personalized advisory. Would you be open to a 15-minute call this week?"));
                case FOLLOW_UP_1 -> new GeneratedMessage(Channel.LINKEDIN, null,
                        "Hi " + firstName + ", just floating this back to the top of your inbox in case it got buried. "
                                + "Happy to share a couple of ideas relevant to " + company + " whenever convenient.");
                case FOLLOW_UP_2 -> new GeneratedMessage(Channel.LINKEDIN, null,
                        "Hi " + firstName + ", following up once more - even a quick 10-minute call could be valuable. "
                                + "Let me know a time that works.");
                case FOLLOW_UP_3 -> new GeneratedMessage(Channel.LINKEDIN, null,
                        "Hi " + firstName + ", I'll leave this here for now - reach out anytime if a wealth conversation "
                                + "becomes useful down the line. Wishing you continued success at " + company + ".");
            };
        }
        return generate(lead, channel);
    }

    @Override
    public GeneratedMessage generate(Lead lead, Channel channel) {
        String firstName = firstName(lead.getName());
        String company = lead.getCompany() == null ? "your organization" : lead.getCompany();
        String role = lead.getDesignation() == null ? "your role" : lead.getDesignation();

        return switch (channel) {
            case EMAIL -> new GeneratedMessage(Channel.EMAIL,
                    "Tailored wealth strategies for leaders like you, " + firstName,
                    ("Hi " + firstName + ",\n\n"
                            + "Congratulations on your work as " + role + " at " + company + ". "
                            + "We partner with accomplished professionals to build and protect long-term wealth "
                            + "through personalized advisory. I'd value 15 minutes to share ideas relevant to your goals.\n\n"
                            + "Would this week suit you for a short call?\n\n"
                            + "Warm regards,\nYour Relationship Manager"));
            case LINKEDIN -> new GeneratedMessage(Channel.LINKEDIN, null,
                    ("Hi " + firstName + ", I admire your journey as " + role + " at " + company + ". "
                            + "I work with senior leaders on bespoke wealth strategies and would love to connect."));
            case WHATSAPP -> new GeneratedMessage(Channel.WHATSAPP, null,
                    ("Hello " + firstName + ", this is your dedicated relationship manager. "
                            + "I'd be glad to share a tailored wealth overview at your convenience. When works for you?"));
            case SMS -> new GeneratedMessage(Channel.SMS, null,
                    ("Hi " + firstName + ", personalized wealth advisory tailored to your goals. "
                            + "Reply YES for a quick call."));
        };
    }

    private String firstName(String name) {
        if (name == null || name.isBlank()) return "there";
        return name.trim().split("\\s+")[0];
    }
}
