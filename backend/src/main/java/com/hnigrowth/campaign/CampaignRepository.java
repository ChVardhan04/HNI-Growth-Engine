package com.hnigrowth.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    long countByStatus(CampaignStatus status);
}
