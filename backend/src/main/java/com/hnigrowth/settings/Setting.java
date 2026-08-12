package com.hnigrowth.settings;

import com.hnigrowth.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Runtime-configurable key/value settings (API keys, webhook URLs, etc.).
 * Values are never hardcoded in code -- they live here or in environment
 * variables, and this table lets an ADMIN change them without a redeploy.
 * Secret values are masked when returned via the API.
 */
@Entity
@Table(name = "settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting extends BaseEntity {

    @Column(name = "setting_key", nullable = false, unique = true)
    private String key;

    @Column(length = 2000)
    private String value;

    private boolean secret;

    private String description;
}
