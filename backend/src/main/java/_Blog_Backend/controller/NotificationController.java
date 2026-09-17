package _Blog_Backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.entity.User;
import _Blog_Backend.service.NotificationService;
import _Blog_Backend.service.RateLimitingService;
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
                    .body(Map.of("message","Too many attempts. Please try again in 15 minutes."));
        }

        return ResponseEntity.ok(notificationService.getUserNotifications(currentUser, cursor));
    }

    @GetMapping("/unreadcount")
    public ResponseEntity<?> getUnreadNotificationsCount(@AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message","Too many attempts. Please try again in 15 minutes."));
        }
        int unreadCount = notificationService.getUnreadNotificationsCount(currentUser);
        return ResponseEntity.ok(unreadCount);
    }

    @PutMapping("/{id}/readOrUnread")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message","Too many attempts. Please try again in 15 minutes."));
        }
        return ResponseEntity.ok(notificationService.markAsReadOrUnread(id, currentUser));
    }
}