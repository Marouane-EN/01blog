package _Blog_Backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import _Blog_Backend.dto.*;

import _Blog_Backend.entity.User;
import _Blog_Backend.service.*;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final LikeService likeService;
    private final RateLimitingService rateLimiter;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestPart("postData") @Valid PostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User author,
            HttpServletRequest httpRequest) {

        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        PostDto createdPost = postService.createPost(request, files, author);
        return ResponseEntity.status(201).body(createdPost);
    }

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        return ResponseEntity.ok(postService.getPostFeed(cursor, currentUserId));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> getPostBySlug(@PathVariable String slug, @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }
        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        PostDto postDto = postService.getPostBySlug(slug, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(postDto);
    }

    @GetMapping("/user/{authorId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable Long authorId,
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        Long currentUserId = (currentUser != null) ? currentUser.getId() : null;
        return ResponseEntity.ok(postService.getPostsByUser(authorId, cursor, currentUserId));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<?> getSubscriptionsFeed(
            @RequestParam(required = false) Long cursor,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        return ResponseEntity.ok(postService.getSubscriptionsFeed(cursor, currentUser.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(
            @RequestParam(name = "q") String keyword,
            @RequestParam(required = false) Long cursor, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(postService.searchPosts(keyword, cursor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        postService.deletePost(id, currentUser);
        return ResponseEntity.ok("Post deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        PostDto updatedPost = postService.updatePost(id, request, currentUser);

        return ResponseEntity.ok(updatedPost);
    }

    @PostMapping("/{postId}/files")
    public ResponseEntity<?> addFileToPost(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) throws IOException {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }
        FileUploadResponse mediaResponse = postService.addFileToPost(postId, file, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaResponse);
    }

    @DeleteMapping("/{postId}/files/{mediaId}")
    public ResponseEntity<?> deleteFileFromPost(
            @PathVariable Long postId,
            @PathVariable Long mediaId,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }
        postService.deleteFileFromPost(postId, mediaId, currentUser);
        return ResponseEntity.ok("Media deleted successfully");
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        return ResponseEntity.ok(likeService.togglePostLike(postId, currentUser));
    }
}
