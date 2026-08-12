package com.hnigrowth.messaging.email;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.outreach.OutreachMessage;
import com.hnigrowth.provider.MessageSendResult;
import com.hnigrowth.provider.MessagingProvider;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Sends EMAIL-channel OutreachMessages via real SMTP (JavaMailSender), from
 * the configured app.mail.from-address (vardhan.chippada@webisdom.ai per
 * project requirement). Refuses to send -- with a clear, specific reason --
 * if the lead has no email on file, exactly like LinkedIn requires a
 * connection first: missing prerequisite info blocks the send rather than
 * silently no-op'ing as "sent".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessagingProvider implements MessagingProvider {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final JavaMailSender mailSender;
    private final EmailProperties props;

    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.EMAIL;
    }

    @Override
    public MessageSendResult send(OutreachMessage message, Lead lead) {
        String to = lead.getEmail();
        if (to == null || to.isBlank()) {
            return MessageSendResult.failed(
                    "This lead has no email address on file -- add one before sending an email.");
        }
        if (!EMAIL_PATTERN.matcher(to).matches()) {
            return MessageSendResult.failed(
                    "The email address on file for this lead (\"" + to + "\") doesn't look valid -- "
                            + "please correct it before sending.");
        }

        String subject = message.getSubject() == null || message.getSubject().isBlank()
                ? "Following up" : message.getSubject();

        if (props.isMockMode()) {
            log.info("[email][MOCK] would send to {} subject=\"{}\"", to, subject);
            return MessageSendResult.ok("mock-email-" + UUID.randomUUID(), "SENT");
        }

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(props.getFromAddress(), props.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message.getBody(), false);
            mailSender.send(mime);
            log.info("[email] sent to {} subject=\"{}\"", to, subject);
            return MessageSendResult.ok("smtp-" + UUID.randomUUID(), "SENT");
        } catch (Exception e) {
            log.error("[email] send failed to {}: {}", to, e.getMessage(), e);
            return MessageSendResult.failed("Email send failed: " + e.getMessage());
        }
    }
}
