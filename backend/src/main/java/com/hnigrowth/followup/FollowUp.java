package com.hnigrowth.followup;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** A scheduled follow-up touchpoint (Day 2 / 5 / 9 / 15) for a lead's outreach sequence. */
@Entity
@Table(name = "follow_ups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUp extends BaseEntity {

    @Column(nullable = false)
    private Long leadId;

    private Long originalMessageId;

    @Column(nullable = false)
    private int sequenceNumber; // 1..4

    @Column(nullable = false)
    private int dayOffset; // 2, 5, 9, 15

    @Column(nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FollowUpStatus status = FollowUpStatus.SCHEDULED;

    private Long generatedMessageId;
}
