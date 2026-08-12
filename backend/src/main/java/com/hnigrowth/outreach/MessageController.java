package com.hnigrowth.outreach;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.outreach.dto.BulkActionResult;
import com.hnigrowth.outreach.dto.BulkConnectRequest;
import com.hnigrowth.outreach.dto.BulkSendRequest;
import com.hnigrowth.outreach.dto.EditMessageRequest;
import com.hnigrowth.outreach.dto.GenerateMessageRequest;
import com.hnigrowth.outreach.dto.RejectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> generate(@Valid @RequestBody GenerateMessageRequest req) {
        return ApiResponse.ok("Draft generated",
                messageService.generate(req.leadId(), req.channel(), req.stage(), req.length()));
    }

    @PostMapping("/{id}/regenerate")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> regenerate(@PathVariable Long id) {
        return ApiResponse.ok("Draft regenerated", messageService.regenerate(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> edit(@PathVariable Long id, @RequestBody EditMessageRequest req) {
        return ApiResponse.ok("Message updated", messageService.edit(id, req));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> approve(@PathVariable Long id, Authentication auth) {
        return ApiResponse.ok("Message approved", messageService.approve(id, auth.getName()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> reject(@PathVariable Long id, @RequestBody RejectRequest req,
                                               Authentication auth) {
        return ApiResponse.ok("Message rejected", messageService.reject(id, req.comments(), auth.getName()));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<OutreachMessage> send(@PathVariable Long id) {
        return ApiResponse.ok("Message sent", messageService.sendApproved(id));
    }

    /** Feature: send a LinkedIn connection request to every selected lead at once,
     * using one shared (placeholder-substituted) default message. */
    @PostMapping("/bulk-connect")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<BulkActionResult> bulkConnect(@Valid @RequestBody BulkConnectRequest req, Authentication auth) {
        BulkActionResult result = messageService.bulkSendConnectionRequests(req.leadIds(), req.messageTemplate(), auth.getName());
        return ApiResponse.ok(result.succeeded() + "/" + result.total() + " connection requests sent", result);
    }

    /** Feature: send every selected message at once (auto-approving any still
     * pending), instead of approving/sending one at a time. */
    @PostMapping("/bulk-send")
    @PreAuthorize("hasAnyRole('ADMIN','RM','MANAGER')")
    public ApiResponse<BulkActionResult> bulkSend(@Valid @RequestBody BulkSendRequest req, Authentication auth) {
        BulkActionResult result = messageService.bulkSend(req.messageIds(), auth.getName());
        return ApiResponse.ok(result.succeeded() + "/" + result.total() + " messages sent", result);
    }

    @GetMapping("/pending")
    public ApiResponse<List<OutreachMessage>> pending() {
        return ApiResponse.ok(messageService.pending());
    }

    @GetMapping("/approved")
    public ApiResponse<List<OutreachMessage>> approved() {
        return ApiResponse.ok(messageService.approvedNotSent());
    }

    @GetMapping("/lead/{leadId}")
    public ApiResponse<List<OutreachMessage>> forLead(@PathVariable Long leadId) {
        return ApiResponse.ok(messageService.forLead(leadId));
    }
}
