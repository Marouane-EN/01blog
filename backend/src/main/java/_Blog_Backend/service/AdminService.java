package _Blog_Backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.AdminReportDto;
import _Blog_Backend.entity.Comment;
import _Blog_Backend.entity.Post;
import _Blog_Backend.entity.Report;
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
    public Page<User> getAllUsersForAdmin(int page, int size) {
        return userRepository.findAllForAdmin(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Post> getAllPostsForAdmin(int page, int size) {
        return postRepository.findAllForAdmin(PageRequest.of(page, size));
    }

    // --- TAKING ACTION ---

    @Transactional
    public void toggleUserBan(Long userId, boolean blockStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setBlocked(blockStatus);
        userRepository.save(user);
    }

    @Transactional
    public void togglePostVisibility(Long postId, boolean hiddenStatus) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        post.setHidden(hiddenStatus);
        postRepository.save(post);
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
    public void hardDeletePostAsAdmin(Long postId) {
        // DANGEROUS: This will wipe the post and all its comments/likes from existence!
        postRepository.deleteById(postId);
    }
}