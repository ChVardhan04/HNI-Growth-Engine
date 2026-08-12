package com.hnigrowth.ai.scoring;

import com.hnigrowth.ai.model.RecommendationType;
import com.hnigrowth.ai.model.ScoringResult;
import com.hnigrowth.lead.Lead;
import com.hnigrowth.lead.LeadSource;
import com.hnigrowth.lead.Tier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Transparent, weighted rule-based ICP scoring. Each factor contributes points
 * and a human-readable reason, so every score is fully explainable.
 */
@Component
public class RuleBasedIcpScoringEngine implements IcpScoringEngine {

    private static final Set<String> SENIOR_TITLES = Set.of(
            "ceo", "founder", "co-founder", "cofounder", "owner", "chairman",
            "managing director", "md", "director", "president", "partner",
            "cfo", "cto", "coo", "cxo", "promoter");
    private static final Set<String> MID_TITLES = Set.of(
            "vp", "vice president", "head", "avp", "general manager", "gm");
    private static final Set<String> PREMIUM_COMPANIES = Set.of(
            "google", "microsoft", "amazon", "apple", "meta", "goldman sachs",
            "mckinsey", "sequoia", "reliance", "tata", "infosys", "adani");
    private static final Set<String> METRO_CITIES = Set.of(
            "mumbai", "delhi", "bengaluru", "bangalore", "hyderabad",
            "chennai", "pune", "gurgaon", "gurugram", "kolkata");
    private static final Set<String> HIGH_VALUE_INDUSTRIES = Set.of(
            "technology", "finance", "banking", "private equity", "venture capital",
            "real estate", "pharmaceutical", "manufacturing");

    @Override
    public ScoringResult score(Lead lead) {
        int score = 0;
        int dataPoints = 0;
        int dataPresent = 0;
        List<String> reasons = new ArrayList<>();

        // Designation (max 25)
        dataPoints++;
        String title = lower(lead.getDesignation());
        if (containsAny(title, SENIOR_TITLES)) { score += 25; dataPresent++; reasons.add("Senior decision-maker (" + lead.getDesignation() + ")"); }
        else if (containsAny(title, MID_TITLES)) { score += 15; dataPresent++; reasons.add("Mid-senior role (" + lead.getDesignation() + ")"); }
        else if (!title.isBlank()) { score += 6; dataPresent++; }

        // Company (max 20)
        dataPoints++;
        String company = lower(lead.getCompany());
        if (containsAny(company, PREMIUM_COMPANIES)) { score += 20; dataPresent++; reasons.add("Premium employer (" + lead.getCompany() + ")"); }
        else if (!company.isBlank()) { score += 10; dataPresent++; reasons.add("Employed at " + lead.getCompany()); }

        // Industry (max 12)
        dataPoints++;
        String industry = lower(lead.getIndustry());
        if (containsAny(industry, HIGH_VALUE_INDUSTRIES)) { score += 12; dataPresent++; reasons.add("High-value industry (" + lead.getIndustry() + ")"); }
        else if (!industry.isBlank()) { score += 5; dataPresent++; }

        // Location (max 10)
        dataPoints++;
        String location = lower(lead.getLocation());
        if (containsAny(location, METRO_CITIES)) { score += 10; dataPresent++; reasons.add("Metro location (" + lead.getLocation() + ")"); }
        else if (!location.isBlank()) { score += 4; dataPresent++; }

        // Years of experience (max 13)
        dataPoints++;
        Integer years = lead.getYearsExperience();
        if (years != null) {
            dataPresent++;
            if (years >= 20) { score += 13; reasons.add(years + " years of experience"); }
            else if (years >= 10) { score += 9; reasons.add(years + " years of experience"); }
            else if (years >= 5) { score += 5; }
            else { score += 2; }
        }

        // LinkedIn presence (max 8)
        dataPoints++;
        if (notBlank(lead.getLinkedinUrl())) { score += 8; dataPresent++; reasons.add("LinkedIn profile present"); }

        // Email presence (max 5)
        dataPoints++;
        if (notBlank(lead.getEmail())) { score += 5; dataPresent++; }

        // Source (max 12)
        dataPoints++;
        LeadSource source = lead.getSource();
        if (source != null) {
            dataPresent++;
            switch (source) {
                case PREMIUM, REFERRAL -> { score += 12; reasons.add("Premium/referral source"); }
                case WEBINAR, EVENT -> score += 8;
                case LINKEDIN, WEBSITE -> score += 6;
                default -> score += 3;
            }
        }

        // Previous engagement / buying signals (max 10)
        dataPoints++;
        int prev = lead.getPreviousEngagementScore();
        if (prev > 0) { score += Math.min(10, prev / 10); dataPresent++; reasons.add("Prior engagement detected"); }

        // Company size (decision-making authority proxy, max 8)
        dataPoints++;
        String companySize = lower(lead.getCompanySize());
        if (companySize.contains("1000") || companySize.contains("501") || companySize.contains("201")) {
            score += 8; dataPresent++; reasons.add("Mid-to-large organization (" + lead.getCompanySize() + " employees)");
        } else if (!companySize.isBlank()) { score += 3; dataPresent++; }

        // Decision-maker score bonus (title + connection strength, max 7)
        dataPoints++;
        if (containsAny(title, SENIOR_TITLES) && "1st".equalsIgnoreCase(lead.getConnectionDegree())) {
            score += 7; dataPresent++; reasons.add("Senior decision-maker with warm network connection");
        } else if (containsAny(title, SENIOR_TITLES)) {
            score += 4; dataPresent++;
        }

        score = Math.min(100, score);
        Tier tier = toTier(score);
        int confidence = (int) Math.round(60 + 40.0 * dataPresent / dataPoints); // 60-100 by completeness
        String reason = reasons.isEmpty()
                ? "Limited profile data available; scored conservatively."
                : "High score because: " + String.join(", ", reasons.subList(0, Math.min(5, reasons.size()))) + ".";

        return new ScoringResult(score, tier, reason, confidence, nextBestAction(tier));
    }

    private Tier toTier(int score) {
        if (score >= 85) return Tier.A_PLUS;
        if (score >= 70) return Tier.A;
        if (score >= 55) return Tier.B;
        if (score >= 40) return Tier.C;
        return Tier.D;
    }

    private RecommendationType nextBestAction(Tier tier) {
        return switch (tier) {
            case A_PLUS -> RecommendationType.SCHEDULE_ADVISOR_MEETING;
            case A -> RecommendationType.PREMIUM_OUTREACH;
            case B -> RecommendationType.EMAIL_AGAIN;
            case C -> RecommendationType.NURTURE;
            case D -> RecommendationType.MOVE_TO_CAMPAIGN;
        };
    }

    private String lower(String s) { return s == null ? "" : s.toLowerCase(); }
    private boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private boolean containsAny(String haystack, Set<String> needles) {
        if (haystack == null || haystack.isBlank()) return false;
        for (String n : needles) if (haystack.contains(n)) return true;
        return false;
    }
}
