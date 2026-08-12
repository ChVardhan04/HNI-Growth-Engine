package com.hnigrowth.settings;

/** Well-known setting keys used across the platform. */
public final class SettingKey {
    public static final String LINKEDIN_BASE_URL = "LINKEDIN_BASE_URL";
    public static final String LINKEDIN_API_KEY = "LINKEDIN_API_KEY";
    public static final String LINKEDIN_ACCOUNT_ID = "LINKEDIN_ACCOUNT_ID";
    public static final String LINKEDIN_WORKFLOW_ID = "LINKEDIN_WORKFLOW_ID";
    public static final String OPENAI_API_KEY = "OPENAI_API_KEY";
    public static final String CLAUDE_API_KEY = "CLAUDE_API_KEY";
    public static final String GEMINI_API_KEY = "GEMINI_API_KEY";
    public static final String SMTP_HOST = "SMTP_HOST";
    public static final String SMTP_USERNAME = "SMTP_USERNAME";
    public static final String SMTP_PASSWORD = "SMTP_PASSWORD";
    public static final String WEBHOOK_URL = "WEBHOOK_URL";
    public static final String HOT_LEAD_THRESHOLD = "HOT_LEAD_THRESHOLD";
    /** Free-text instructions the RM/sales lead can edit from Settings, prepended to
     * every AI message-generation prompt (project spec: "give instructions before
     * generating messages"). Can include general tone guidance and industry-specific
     * notes, e.g. "For BFSI prospects, reference regulatory trust and compliance.
     * For Technology prospects, reference innovation velocity and scale." */
    public static final String MESSAGE_GENERATION_INSTRUCTIONS = "MESSAGE_GENERATION_INSTRUCTIONS";

    private SettingKey() {}
}
