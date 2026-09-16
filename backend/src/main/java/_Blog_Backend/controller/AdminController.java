package _Blog_Backend.controller;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.dto.ResolveRequest;
import _Blog_Backend.service.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/reports")
    public ResponseEntity<?> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getPendingReports(PageRequest.of(page, size)));
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<String> resolveReport(
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveRequest request) {

        adminService.resolveReport(reportId, request.action());
        return ResponseEntity.ok("Report resolved successfully.");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = 50, message = "Page size cannot exceed 50 items") @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(adminService.getAllUsersForAdmin(page, size, search));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = 50, message = "Page size cannot exceed 50 items") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getUserPostsForAdmin(userId, page, size));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> getPostsDirectory(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = 50, message = "Page size cannot exceed 50 items") @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(adminService.getAllPostsForAdmin(page, size, search));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPostDetails(
            @PathVariable Long postId) {
        return ResponseEntity.ok(adminService.getPostDetails(postId));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getPostComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @Max(value = 50, message = "Page size cannot exceed 50 items") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getPostComments(postId, page, size));
    }

    @PutMapping("/users/{userId}/ban")
    public ResponseEntity<?> toggleUserBan(@PathVariable Long userId) {
        String message = adminService.toggleUserBan(userId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> toggleUserDelete(@PathVariable Long userId) {
        String message = adminService.toggleUserDelete(userId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/posts/{postId}/hide")
    public ResponseEntity<?> togglePostVisibility(@PathVariable Long postId) {
        String message = adminService.togglePostVisibility(postId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<?> hardDeletePost(@PathVariable Long postId) {
        String message = adminService.hardDeletePost(postId);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        adminService.deleteCommentAsAdmin(commentId);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully."));
    }

}
