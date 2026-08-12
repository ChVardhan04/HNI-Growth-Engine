package com.hnigrowth.intent;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.lead.LeadMapper;
import com.hnigrowth.lead.dto.LeadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/engagement")
@RequiredArgsConstructor
public class EngagementController {

    private final IntentService intentService;
    private final LeadMapper leadMapper;

    /** Record an engagement activity; returns the lead with refreshed intent + recommendation. */
    @PostMapping("/{leadId}/track")
    public ApiResponse<LeadResponse> track(@PathVariable Long leadId,
                                           @RequestParam ActivityType type,
                                           @RequestParam(required = false) String description) {
        var lead = intentService.trackActivity(leadId, type, description);
        return ApiResponse.ok("Activity tracked", leadMapper.toResponse(lead));
    }

    @GetMapping("/{leadId}/timeline")
    public ApiResponse<List<Activity>> timeline(@PathVariable Long leadId) {
        return ApiResponse.ok(intentService.timeline(leadId));
    }
}
