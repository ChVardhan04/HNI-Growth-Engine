package com.hnigrowth.ai.recommendation;

import com.hnigrowth.ai.model.Recommendation;
import com.hnigrowth.ai.model.RecommendationType;
import com.hnigrowth.intent.IntentBand;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.Tier;
import org.springframework.stereotype.Component;

/** Combines ICP tier and intent band into an explainable next-best-action. */
@Component
public class RuleBasedRecommendationEngine implements RecommendationEngine {

    @Override
    public Recommendation recommend(Lead lead, int intentScore, IntentBand band) {
        Tier tier = lead.getTier() == null ? Tier.C : lead.getTier();

        if (band == IntentBand.HOT) {
            return new Recommendation(RecommendationType.BOOK_ADVISOR,
                    "Intent is HOT (" + intentScore + "/100); the prospect is actively engaging. Book an advisor meeting immediately.",
                    95);
        }
        if (band == IntentBand.HIGH) {
            return new Recommendation(RecommendationType.CALL_CUSTOMER,
                    "High intent (" + intentScore + "/100). A direct call will capitalize on current interest.",
                    88);
        }
        if (band == IntentBand.MEDIUM && (tier == Tier.A_PLUS || tier == Tier.A)) {
            return new Recommendation(RecommendationType.PREMIUM_OUTREACH,
                    "Premium ICP tier " + tier + " with medium intent. Warrants a high-touch premium outreach.",
                    80);
        }
        if (band == IntentBand.MEDIUM) {
            return new Recommendation(RecommendationType.EMAIL_AGAIN,
                    "Medium intent (" + intentScore + "/100). Send a value-led follow-up email to advance engagement.",
                    72);
        }
        if (band == IntentBand.LOW) {
            return new Recommendation(RecommendationType.LINKEDIN_FOLLOWUP,
                    "Low but non-zero intent. A soft LinkedIn touch keeps the prospect warm.",
                    65);
        }
        if (tier == Tier.D) {
            return new Recommendation(RecommendationType.REJECT_LEAD,
                    "Low ICP tier D and cold intent. Deprioritize to protect RM capacity.",
                    70);
        }
        return new Recommendation(RecommendationType.NURTURE,
                "Cold intent. Keep in an automated nurture cycle and re-evaluate later.",
                68);
    }
}
