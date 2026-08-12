package com.hnigrowth.user;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Advisor-routing attributes (used by AdvisorRoutingService)
    private String region;
    private String language;
    private String specialization;

    @Builder.Default
    private boolean available = true;

    @Builder.Default
    private int activeLeadCount = 0;

    @Builder.Default
    private boolean enabled = true;
}
