package com.hnigrowth.advisor;

import com.hnigrowth.lead.Lead;
import com.hnigrowth.user.Role;
import com.hnigrowth.user.User;
import com.hnigrowth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Assigns a Relationship Manager to a lead based on region, language,
 * specialization, availability and current load.
 */
@Service
@RequiredArgsConstructor
public class AdvisorRoutingService {

    private final UserRepository userRepository;

    /** @return the selected RM, or empty if none available. */
    public Optional<User> routeLead(Lead lead) {
        List<User> candidates = userRepository.findByRoleAndAvailableTrue(Role.RM);
        if (candidates.isEmpty()) return Optional.empty();

        return candidates.stream()
                .max(Comparator
                        .comparingInt((User rm) -> matchScore(rm, lead))
                        // prefer the least-loaded RM among equal matches
                        .thenComparing(rm -> -rm.getActiveLeadCount()));
    }

    private int matchScore(User rm, Lead lead) {
        int score = 0;
        if (matches(rm.getRegion(), lead.getLocation())) score += 3;
        if (rm.getLanguage() != null && !rm.getLanguage().isBlank()) score += 1;
        if (matches(rm.getSpecialization(), lead.getIndustry())) score += 2;
        return score;
    }

    private boolean matches(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) return false;
        return a.toLowerCase().contains(b.toLowerCase()) || b.toLowerCase().contains(a.toLowerCase());
    }
}
