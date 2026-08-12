package com.hnigrowth.linkedin.exceptions;

/** Raised when a live call is attempted but BASE_URL/API_KEY/ACCOUNT_ID are not set. */
public class LinkedInNotConfiguredException extends RuntimeException {
    public LinkedInNotConfiguredException() {
        super("LinkedIn API is not configured. Set linkedin.api.base-url, api-key and account-id " +
                "(env: LINKEDIN_BASE_URL, LINKEDIN_API_KEY, LINKEDIN_ACCOUNT_ID) to go live. " +
                "Running in mock mode until then.");
    }
}
