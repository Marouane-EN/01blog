package _Blog_Backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.service.RateLimitingService;
import _Blog_Backend.service.TagService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final RateLimitingService rateLimiter;

    @GetMapping("/trending")
    public ResponseEntity<?> getTrendingTags(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message","Too many attempts. Please try again in 15 minutes."));
        }

        int cappedLimit = Math.min(Math.max(limit, 1), 20);
        return ResponseEntity.ok(tagService.getTrendingTags(cappedLimit));
    }
}
