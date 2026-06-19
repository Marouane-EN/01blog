package _Blog_Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.NotificationRepository;
import _Blog_Backend.service.*;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final RateLimitingService rateLimiter;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser, cursor));
    }

    @PutMapping("/{id}/readOrUnread")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsReadOrUnread(id);
        return ResponseEntity.ok().build();
    }
}