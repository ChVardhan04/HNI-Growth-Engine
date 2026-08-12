package com.hnigrowth.linkedin;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.linkedin.dto.LinkedInProfileDto;
import com.hnigrowth.linkedin.dto.LinkedInSearchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exposes ad-hoc LinkedIn search for testing/preview. Campaign-driven search
 * automation runs through LinkedInWorkflowService via CampaignService.start().
 * No HTTP calls happen here directly -- delegated to LinkedInService.
 */
@RestController
@RequestMapping("/api/linkedin")
@RequiredArgsConstructor
public class LinkedInController {

    private final LinkedInService linkedInService;

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','RM')")
    public ApiResponse<List<LinkedInProfileDto>> search(@Valid @RequestBody LinkedInSearchRequest request) {
        return ApiResponse.ok(linkedInService.search(request));
    }
}
