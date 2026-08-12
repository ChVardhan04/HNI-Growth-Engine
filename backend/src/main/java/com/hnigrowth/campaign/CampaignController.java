package com.hnigrowth.campaign;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.campaign.dto.CampaignRequest;
import com.hnigrowth.lead.dto.LeadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RM')")
    public ApiResponse<Campaign> create(@Valid @RequestBody CampaignRequest req, Authentication auth) {
        return ApiResponse.ok("Campaign created", campaignService.create(req, auth.getName()));
    }

    /**
     * Launches the campaign: automatically triggers LinkedIn search, enrichment,
     * lead storage and AI scoring end to end. No manual import step.
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RM')")
    public ApiResponse<List<LeadResponse>> start(@PathVariable Long id) {
        List<LeadResponse> imported = campaignService.start(id);
        return ApiResponse.ok(imported.size() + " leads imported from LinkedIn and AI-scored", imported);
    }

    @GetMapping
    public ApiResponse<List<Campaign>> list() { return ApiResponse.ok(campaignService.list()); }

    @GetMapping("/{id}")
    public ApiResponse<Campaign> get(@PathVariable Long id) { return ApiResponse.ok(campaignService.get(id)); }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Campaign> status(@PathVariable Long id, @RequestParam CampaignStatus status) {
        return ApiResponse.ok("Status updated", campaignService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return ApiResponse.ok("Campaign deleted", null);
    }
}
