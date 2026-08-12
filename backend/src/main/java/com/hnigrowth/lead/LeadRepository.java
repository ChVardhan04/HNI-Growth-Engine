package com.hnigrowth.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    Page<Lead> findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name, String company, String email, Pageable pageable);

    List<Lead> findByStatus(LeadStatus status);

    long countByStatus(LeadStatus status);

    long countByIntentBand(com.hnigrowth.intent.IntentBand band);

    long countByTier(Tier tier);

    List<Lead> findTop5ByOrderByIcpScoreDesc();
}
