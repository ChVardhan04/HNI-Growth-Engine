package com.hnigrowth.linkedin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * External LinkedIn access configuration via Linked API (linkedapi.io).
 * All values are left blank by default and MUST be supplied via environment
 * variables / application.yml before go-live.
 *
 * Linked API auth model: every request needs two headers --
 *   linked-api-token    (this account's overall API access token -> apiKey)
 *   identification-token (token for the specific connected LinkedIn account -> accountId)
 * See https://linkedapi.io/docs/making-requests/
 */
@Component
@ConfigurationProperties(prefix = "linkedin.api")
public class LinkedInApiProperties {

    /** e.g. https://api{subdomain}.unipile.com:{port} */
    private String baseUrl = "";

    /** Provider API key / access token. */
    private String apiKey = "";

    /** Connected LinkedIn account id inside the provider (Unipile account_id). */
    private String accountId = "";

    /** Workflow / automation id if the provider supports server-side workflows. */
    private String workflowId = "";

    /** Toggles mock responses when credentials are not yet configured. */
    private boolean mockMode = true;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public boolean isMockMode() { return mockMode; }
    public void setMockMode(boolean mockMode) { this.mockMode = mockMode; }

    public boolean isConfigured() {
        return baseUrl != null && !baseUrl.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && accountId != null && !accountId.isBlank();
    }
}
