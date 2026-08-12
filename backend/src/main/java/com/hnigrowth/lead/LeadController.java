package com.hnigrowth.lead;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.lead.dto.LeadRequest;
import com.hnigrowth.lead.dto.LeadResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<LeadResponse> create(@Valid @RequestBody LeadRequest request, Authentication auth) {
        return ApiResponse.ok("Lead created and scored", leadService.create(request, auth.getName()));
    }

    @GetMapping
    public ApiResponse<Page<LeadResponse>> list(@RequestParam(required = false) String search,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(leadService.list(search, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<LeadResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(leadService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<LeadResponse> update(@PathVariable Long id, @Valid @RequestBody LeadRequest request) {
        return ApiResponse.ok("Lead updated", leadService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return ApiResponse.ok("Lead deleted", null);
    }

    @PostMapping("/{id}/rescore")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<LeadResponse> rescore(@PathVariable Long id) {
        return ApiResponse.ok("Lead re-scored", leadService.rescore(id));
    }
}
