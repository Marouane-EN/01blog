package _Blog_Backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.AdminCommentDto;
import _Blog_Backend.dto.AdminPostDetailsDto;
import _Blog_Backend.dto.AdminPostDto;
import _Blog_Backend.dto.AdminReportDto;
import _Blog_Backend.dto.AdminUserDto;
import _Blog_Backend.entity.Comment;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.PostMedia;
import _Blog_Backend.entity.Report;
import _Blog_Backend.entity.Tag;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.CommentRepository;
import _Blog_Backend.repository.PostRepository;
import _Blog_Backend.repository.ReportRepository;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.types.AdminAction;
import _Blog_Backend.types.ReportType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminReportDto> getPendingReports(Pageable pageable) {
        return reportRepository.findByIsResolvedFalseOrderByCreatedAtAsc(pageable)
                .map(report -> new AdminReportDto(
                        report.getId(),
                        report.getReporter().getUsername(),
                        report.getReported().getUsername(),
                        report.getReportType().name(),
                        report.getTargetId(),
                        report.getReason(),
                        report.getCreatedAt()));
    }

    @Transactional
    public void resolveReport(Long reportId, AdminAction action) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));

        if (report.isResolved()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report is already resolved.");
        }

        switch (action) {
            case BAN_USER -> {
                User badActor = report.getReported();
                badActor.setBlocked(true);
            }
            case HIDE_CONTENT -> {
                if (report.getReportType() == ReportType.POST) {
                    Post post = postRepository.findById(report.getTargetId()).orElseThrow();
                    post.setHidden(true);
                } else if (report.getReportType() == ReportType.COMMENT) {
                    Comment comment = commentRepository.findById(report.getTargetId()).orElseThrow();
                    comment.setDeleted(true);
                    comment.setContent("[This comment was removed by an Administrator]");
                }
            }
            case DISMISS -> {
                // Do nothing to the user or content, just close the ticket.
            }
        }

        report.setResolved(true);
        reportRepository.save(report);
    }

    // --- FETCHING DATA ---
    @Transactional(readOnly = true)
    public Page<AdminUserDto> getAllUsersForAdmin(int page, int size, String searchKeyword) {

        // Sort by newest users first
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> usersPage;

        // If the admin typed a search term, filter the list!
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            usersPage = userRepository.findByUsernameContainingIgnoreCase(searchKeyword, pageable);
        } else {
            // Otherwise, just return everyone
            usersPage = userRepository.findAll(pageable);
        }

        // Map the raw User entities to the secure Admin DTO
        return usersPage.map(user -> new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isBlocked(),
                user.isActive(),
                user.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<AdminPostDto> getAllPostsForAdmin(int page, int size, String searchKeyword) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> postsPage;

        if (searchKeyword != null && !searchKeyword.isBlank()) {
            postsPage = postRepository.searchAllForAdmin(searchKeyword, pageable);
        } else {
            postsPage = postRepository.findAllForAdmin(pageable);
        }

        return postsPage.map(post -> new AdminPostDto(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl(),
                post.isHidden(),
                post.getLikesCount(),
                post.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public AdminPostDetailsDto getPostDetails(Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        List<String> mediaUrls = post.getMediaList().stream()
                .map(PostMedia::getMediaUrl)
                .toList();
        List<String> tags = post.getTags().stream()
                .map(Tag::getName)
                .toList();

        return new AdminPostDetailsDto(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl(),
                post.isHidden(),
                tags,
                mediaUrls,
                post.getLikesCount(),
                post.getCreatedAt());
    }

    public Page<AdminCommentDto> getPostComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return commentRepository.findByPostId(postId, pageable)
                .map(comment -> new AdminCommentDto(
                        comment.getId(),
                        comment.getAuthor().getUsername(),
                        comment.getAuthor().getProfilePictureUrl(),
                        comment.getContent(),
                        comment.isDeleted(),
                        comment.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public Page<AdminPostDto> getUserPostsForAdmin(Long authorId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Uses the Native SQL Skeleton Key!
        Page<Post> postsPage = postRepository.findByAuthorIdForAdmin(authorId, pageable);

        return postsPage.map(post -> new AdminPostDto(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getUsername(),
                post.getAuthor().getProfilePictureUrl(),
                post.isHidden(),
                post.getLikesCount(),
                post.getCreatedAt()));
    }

    // --- TAKING ACTION ---

    @Transactional
    public String toggleUserBan(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if ("ADMIN".equals(String.valueOf(user.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot ban an admin.");
        }
        user.setBlocked(!user.isBlocked());

        // Hibernate automatically saves the change when the transaction ends!
        return user.isBlocked() ? "User has been banned." : "User has been unbanned.";
    }

    @Transactional
    public String togglePostVisibility(Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        post.setHidden(!post.isHidden());

        // Because we fetched this with Native SQL, it is safer to explicitly call
        // save()
        postRepository.save(post);

        return post.isHidden() ? "Post has been hidden from the public." : "Post has been restored.";
    }

    @Transactional
    public void deleteCommentAsAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        // Admins use the soft-delete just like normal users to preserve the reply tree!
        comment.setDeleted(true);
        comment.setContent("[This comment was removed by an Administrator]");
        commentRepository.save(comment);
    }

    @Transactional
    public String hardDeletePost(Long postId) {
        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        postRepository.delete(post);

        return "Post has been permanently deleted from the database.";
    }

    @Transactional
    public String toggleUserDelete(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if ("ADMIN".equals(String.valueOf(user.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete an admin.");
        }
        user.setActive(!user.isActive());

        return user.isActive() ? "User account has been restored." : "User account has been deactivated.";
    }

}