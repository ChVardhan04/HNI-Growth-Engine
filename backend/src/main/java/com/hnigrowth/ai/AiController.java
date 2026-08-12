package com.hnigrowth.ai;

import com.hnigrowth.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Exposes AI-layer metadata (e.g. the active provider) to the UI. */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AIService aiService;

    @GetMapping("/provider")
    public ApiResponse<Map<String, String>> provider() {
        return ApiResponse.ok(Map.of("activeProvider", aiService.activeProvider()));
    }
}
