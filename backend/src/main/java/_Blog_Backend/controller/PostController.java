package _Blog_Backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import _Blog_Backend.dto.PostDto;
import _Blog_Backend.dto.PostRequest;
import _Blog_Backend.entity.User;
import _Blog_Backend.service.PostService;
import _Blog_Backend.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final RateLimitingService rateLimiter;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestPart("postData") @Valid PostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User author,
            HttpServletRequest httpRequest) {

        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        PostDto createdPost = postService.createPost(request, images, author);
        return ResponseEntity.status(201).body(createdPost);
    }

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        return ResponseEntity.ok(postService.getPostFeed(cursor));
    }
}
