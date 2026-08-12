package com.hnigrowth.notification;

import com.hnigrowth.common.dto.ApiResponse;
import com.hnigrowth.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<List<Notification>> mine(Authentication auth) {
        Long rmId = currentUserId(auth);
        return ApiResponse.ok(notificationService.forRm(rmId));
    }

    @GetMapping("/me/unread")
    public ApiResponse<List<Notification>> myUnread(Authentication auth) {
        Long rmId = currentUserId(auth);
        return ApiResponse.ok(notificationService.unreadForRm(rmId));
    }

    private Long currentUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .map(u -> u.getId())
                .orElse(-1L);
    }

    @GetMapping("/rm/{rmId}")
    public ApiResponse<List<Notification>> forRm(@PathVariable Long rmId) {
        return ApiResponse.ok(notificationService.forRm(rmId));
    }

    @GetMapping("/rm/{rmId}/unread")
    public ApiResponse<List<Notification>> unread(@PathVariable Long rmId) {
        return ApiResponse.ok(notificationService.unreadForRm(rmId));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.ok("Marked read", null);
    }
}
