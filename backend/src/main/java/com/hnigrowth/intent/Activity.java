package com.hnigrowth.intent;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/** A single tracked engagement event for a lead. */
@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity extends BaseEntity {

    @Column(nullable = false)
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    private String description;

    /** Point contribution of this activity toward the intent score. */
    private int weightApplied;
}
