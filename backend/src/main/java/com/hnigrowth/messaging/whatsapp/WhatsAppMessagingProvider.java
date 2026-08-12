package com.hnigrowth.messaging.whatsapp;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.outreach.OutreachMessage;
import com.hnigrowth.provider.MessageSendResult;
import com.hnigrowth.provider.MessagingProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Sends WHATSAPP-channel OutreachMessages via the WhatsApp Business Cloud API
 * (Meta). TODO(integration): base-url/phone-number-id/access-token are
 * placeholders (see WhatsAppProperties) until a WhatsApp Business account is
 * provisioned -- the request-building code below follows Meta's documented
 * /messages endpoint shape, but hasn't been exercised against a live account.
 *
 * Refuses to send -- with a clear reason -- if the lead has no phone number
 * on file, exactly like LinkedIn requires a connection and Email requires an
 * address: missing prerequisite info blocks the send with an actionable
 * message rather than silently no-op'ing as "sent". The RM can add a phone
 * number to the lead afterward and retry.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppMessagingProvider implements MessagingProvider {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    private final WhatsAppProperties props;

    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.WHATSAPP;
    }

    @Override
    public MessageSendResult send(OutreachMessage message, Lead lead) {
        String phone = lead.getPhone();
        if (phone == null || phone.isBlank()) {
            return MessageSendResult.failed(
                    "This lead has no phone number on file -- add one to the lead, then retry sending via WhatsApp.");
        }
        String normalized = phone.replaceAll("[\\s()-]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            return MessageSendResult.failed(
                    "The phone number on file for this lead (\"" + phone + "\") doesn't look valid -- "
                            + "please correct it before sending.");
        }

        if (props.isMockMode() || !props.isConfigured()) {
            log.info("[whatsapp][MOCK] would send to {}", normalized);
            return MessageSendResult.ok("mock-whatsapp-" + UUID.randomUUID(), "SENT");
        }

        try {
            RestClient client = RestClient.builder().baseUrl(props.getBaseUrl()).build();
            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", normalized,
                    "type", "text",
                    "text", Map.of("body", message.getBody())
            );
            client.post()
                    .uri("/{phoneNumberId}/messages", props.getPhoneNumberId())
                    .header("Authorization", "Bearer " + props.getAccessToken())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("[whatsapp] sent to {}", normalized);
            return MessageSendResult.ok("wa-" + UUID.randomUUID(), "SENT");
        } catch (Exception e) {
            log.error("[whatsapp] send failed to {}: {}", normalized, e.getMessage(), e);
            return MessageSendResult.failed("WhatsApp send failed: " + e.getMessage());
        }
    }
}
