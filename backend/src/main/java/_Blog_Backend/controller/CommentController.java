package _Blog_Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.dto.CommentDto;
import _Blog_Backend.dto.CommentRequest;
import _Blog_Backend.entity.User;
import _Blog_Backend.service.CommentService;
import _Blog_Backend.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final RateLimitingService rateLimiter;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(@Valid @RequestBody CommentRequest request, @PathVariable Long postId,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        CommentDto createdComment = commentService.createComment(request, postId, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId, @RequestParam(required = false) Long cursor,
            HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);

        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }
        return ResponseEntity.ok(commentService.getCommentsForPost(postId, cursor));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@Valid @RequestBody CommentRequest request,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);
        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        commentService.updateComment(commentId, request.content(), currentUser);

        return ResponseEntity.ok("Comment updated successfully!");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User currentUser) {

        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }
}
