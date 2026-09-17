package _Blog_Backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.dto.ReportRequest;
import _Blog_Backend.entity.User;
import _Blog_Backend.service.RateLimitingService;
import _Blog_Backend.service.ReportService;
import _Blog_Backend.types.ReportType;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final RateLimitingService rateLimiter;

    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<?> reportPost(
            @PathVariable Long postId,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        reportService.submitReport(currentUser, postId, ReportType.POST, request.reason());
        return ResponseEntity.ok(Map.of("message", "Post reported successfully."));
    }

    @PostMapping("/comments/{commentId}/reports")
    public ResponseEntity<?> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        reportService.submitReport(currentUser, commentId, ReportType.COMMENT, request.reason());
        return ResponseEntity.ok(Map.of("message", "Comment reported successfully."));
    }

    @PostMapping("/users/{userId}/reports")
    public ResponseEntity<?> reportUser(
            @PathVariable Long userId,
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        reportService.submitReport(currentUser, userId, ReportType.USER, request.reason());
        return ResponseEntity.ok(Map.of("message", "Profile reported successfully."));
    }

}