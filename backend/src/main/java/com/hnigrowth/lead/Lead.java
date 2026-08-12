package com.hnigrowth.lead;

import com.hnigrowth.ai.model.RecommendationType;
import com.hnigrowth.common.entity.BaseEntity;
import com.hnigrowth.intent.IntentBand;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead extends BaseEntity {

    // --- Profile ---
    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;
    private String company;
    private String designation;
    private String industry;
    private String location;
    private Integer yearsExperience;
    private String linkedinUrl;

    // --- LinkedIn-sourced enrichment fields ---
    private String profileImageUrl;
    private String currentPosition;
    private String companySize;
    private String connectionDegree;

    /** CONNECTED / PENDING / NOT_CONNECTED / UNKNOWN -- kept in sync whenever
     * LinkedInMessagingProvider checks status before sending a message. Direct
     * messages are only ever attempted when this is CONNECTED. */
    private String connectionStatus;
    private String providerProfileId; // LinkedIn/Unipile profile id, used to send messages
    private Long campaignId;

    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String education;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeadPriority priority = LeadPriority.MEDIUM;

    /** Email of the RM who owns/created the lead. */
    private String owner;

    /** Advisor (RM) assigned by the routing engine. */
    private Long assignedRmId;
    private String assignedRmName;

    @Column(length = 2000)
    private String notes;

    @Builder.Default
    private int previousEngagementScore = 0;

    // --- AI: ICP scoring (explainable) ---
    private Integer icpScore;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Column(length = 1000)
    private String icpReason;
    private Integer icpConfidence;

    // --- AI: intent ---
    @Builder.Default
    private int intentScore = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IntentBand intentBand = IntentBand.COLD;

    // --- AI: recommendation (explainable) ---
    @Enumerated(EnumType.STRING)
    private RecommendationType nextBestAction;

    @Column(length = 1000)
    private String recommendationReason;
}
