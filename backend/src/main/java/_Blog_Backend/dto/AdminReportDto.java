package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record AdminReportDto(
        Long reportId,
        String reporterUsername,
        String reportedUsername,
        String reportType,
        Long targetId,
        String reason,
        LocalDateTime createdAt) {
}