package _Blog_Backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import _Blog_Backend.entity.*;
import _Blog_Backend.repository.*;
import _Blog_Backend.types.ReportType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void submitReport(User reporter, Long targetId, ReportType type, String reason) {

        if (reportRepository.existsByReporterIdAndTargetIdAndReportType(reporter.getId(), targetId, type)) {
            return;
        }

        User reportedUser;

        switch (type) {
            case USER -> {
                reportedUser = userRepository.findById(targetId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            }
            case POST -> {
                Post post = postRepository.findById(targetId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
                reportedUser = post.getAuthor();
            }
            case COMMENT -> {
                Comment comment = commentRepository.findById(targetId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
                reportedUser = comment.getAuthor();
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report type");
        }

        if ("ADMIN".equals(String.valueOf(reportedUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot report an admin.");
        }

        if (reporter.getId().equals(reportedUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot report yourself.");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reportedUser)
                .reportType(type)
                .targetId(targetId)
                .reason(reason)
                .build();

        reportRepository.save(report);
    }
}