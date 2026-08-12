package com.hnigrowth.settings;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.settings.dto.SettingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ApiResponse<List<Setting>> list() { return ApiResponse.ok(settingsService.list()); }

    @PutMapping
    public ApiResponse<Setting> upsert(@RequestBody SettingRequest req) {
        return ApiResponse.ok("Setting saved", settingsService.upsert(req.key(), req.value(), req.secret(), req.description()));
    }
}
