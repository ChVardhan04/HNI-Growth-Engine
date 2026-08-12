package com.hnigrowth.audit;

import com.hnigrowth.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRepository auditRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE','MANAGER')")
    public ApiResponse<Page<AuditLog>> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(auditRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)));
    }
}
