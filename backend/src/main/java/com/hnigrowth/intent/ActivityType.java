package com.hnigrowth.intent;

/** Engagement activities and their contribution to the intent score. */
public enum ActivityType {
    EMAIL_OPEN(10),
    EMAIL_CLICK(20),
    PDF_DOWNLOAD(15),
    WEBSITE_VISIT(10),
    LINKEDIN_CLICK(15),
    WHATSAPP_REPLY(25),
    WEBINAR(30),
    CALL(35),
    MEETING(40),
    DEMO(40),
    ADVISOR_BOOKING(45),

    // LinkedIn/social engagement activities (project spec section 11-12)
    CONNECTION_SENT(5),
    CONNECTION_ACCEPTED(20),
    VIEWED_PROFILE(8),
    VIEWED_MESSAGE(5),
    REPLIED(30),
    POSITIVE_REPLY(35),
    MEETING_REQUESTED(45),
    PROFILE_CLICK(8),
    REPEATED_REPLY(20),
    MULTIPLE_ENGAGEMENT(15);

    private final int weight;

    ActivityType(int weight) { this.weight = weight; }

    public int getWeight() { return weight; }
}
