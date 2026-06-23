package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record AdminCommentDto(
                Long id,
                String authorUsername,
                String authorProfilePictureUrl,
                String content,
                boolean isDeleted,
                LocalDateTime createdAt) {
}