package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDto(
        Long id,
        String content,
        AuthorDto author,
        List<CommentDto> replies,
        int likeCount,
        boolean likedByCurrentUser,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}