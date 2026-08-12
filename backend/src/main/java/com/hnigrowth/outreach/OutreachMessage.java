package com.hnigrowth.outreach;

import com.hnigrowth.ai.model.Channel;
import com.hnigrowth.ai.model.MessageStage;
import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "outreach_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutreachMessage extends BaseEntity {

    @Column(nullable = false)
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    private String subject;

    @Column(length = 4000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING_APPROVAL;

    @Builder.Default
    private boolean aiGenerated = true;

    @Column(length = 1000)
    private String rmComments;

    private String approvedBy;
    private Instant sentAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStage stage = MessageStage.FIRST_MESSAGE;

    /** Provider-side message/connection id once sent (e.g. Unipile message id). */
    private String providerMessageId;

    private int retryCount;

    @Column(columnDefinition = "TEXT")
    private String lastError;
}
