package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDto(
        Long id,
        String content,
        UserProfileDTO author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<CommentDto> replies) {
}