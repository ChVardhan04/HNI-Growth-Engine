package com.hnigrowth.provider;

import com.hnigrowth.lead.Lead;

/** Abstraction for enriching a lead with additional third-party data. */
public interface EnrichmentProvider {
    Lead enrich(Lead lead);
}
