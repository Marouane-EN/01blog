package _Blog_Backend.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.SubscriptionResponse;
import _Blog_Backend.dto.UserDto;
import _Blog_Backend.entity.User;
import _Blog_Backend.service.RateLimitingService;
import _Blog_Backend.service.SubscriptionService;
import _Blog_Backend.service.UserService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final RateLimitingService rateLimiter;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest)
            throws IOException {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }
        UserDto userDto = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping(value = "/me/avatar", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) throws IOException {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }
        return ResponseEntity.ok(Map.of("avatarUrl", userService.updateAvatar(file, currentUser)));
    }

    @PostMapping("/{username}/subscribe")
    public ResponseEntity<?> subscribeToUser(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        SubscriptionResponse response = subscriptionService.toggleSubscription(username, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        CursorResponse<UserDto> response = subscriptionService.getFollowers(userId, cursor);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<?> getFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        CursorResponse<UserDto> response = subscriptionService.getFollowing(userId, cursor);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(
            @PathVariable String username, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        return ResponseEntity.ok(userService.getPublicProfile(username));
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularUsers(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        int cappedLimit = Math.min(Math.max(limit, 1), 20);
        return ResponseEntity.ok(userService.getPopularUsers(cappedLimit));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam(name = "q") String keyword,
            @RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many attempts. Please try again in 15 minutes."));
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(userService.searchUsers(keyword, cursor));
    }

}