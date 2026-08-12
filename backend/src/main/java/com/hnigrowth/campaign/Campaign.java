package com.hnigrowth.campaign;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String type;           // e.g. EMAIL, WEBINAR, NURTURE
    private String audience;       // target segment description
    private String owner;          // RM email who created/owns the campaign

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    private Instant scheduledAt;
    private Instant startedAt;

    // --- LinkedIn search targeting criteria (project spec section 1) ---
    private String industry;
    private String designation;
    private String location;
    private Integer minExperience;
    private Integer maxExperience;

    @ElementCollection
    @CollectionTable(name = "campaign_keywords", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "keyword")
    @Builder.Default
    private List<String> keywords = List.of();

    private String companyName;
    private String companySize;
    private Integer minEmployeeCount;
    private Integer maxEmployeeCount;

    @ElementCollection
    @CollectionTable(name = "campaign_technologies", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "technology")
    @Builder.Default
    private List<String> technologies = List.of();

    private String revenue; // optional revenue-band filter

    /** Set when status is FAILED, so the RM can see why the LinkedIn search didn't run. */
    @Column(length = 1000)
    private String failureReason;

    /** Optional: paste a LinkedIn/Sales Navigator search results URL (after configuring
     * filters directly on linkedin.com) to bypass the structured filters above entirely.
     * Most reliable way to target a search since it sidesteps Linked API's strict
     * industry/location enum matching. */
    @Column(length = 2000)
    private String customSearchUrl;

    // --- Import stats, populated once the campaign is started ---
    @Builder.Default private int leadsImported = 0;

    // Performance metrics
    @Builder.Default private int sent = 0;
    @Builder.Default private int opened = 0;
    @Builder.Default private int clicked = 0;
    @Builder.Default private int converted = 0;

    // AI campaign recommendation (module 6)
    private String recommendedBestTime;
    private String recommendedAudience;
    private Integer expectedConversionRate;
}
