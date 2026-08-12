package com.hnigrowth.outreach.dto;

import java.util.List;

public record BulkActionResult(int total, int succeeded, int failed, List<BulkItemResult> results) {
    public record BulkItemResult(Long id, boolean success, String error) {}
}
