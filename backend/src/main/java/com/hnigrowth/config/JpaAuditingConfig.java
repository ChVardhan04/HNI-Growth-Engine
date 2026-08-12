package com.hnigrowth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Enables @CreatedDate / @LastModifiedDate population on BaseEntity. */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
