package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record AdminPostDto(
        Long id,
        String title,
        String authorUsername,
        String authorProfilePictureUrl,
        boolean isHidden,
        int likesCount,
        LocalDateTime createdAt) {
}