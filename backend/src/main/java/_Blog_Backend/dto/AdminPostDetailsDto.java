package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPostDetailsDto(
        Long id,
        String title,
        String description,
        String authorUsername,
        String authorProfilePictureUrl,
        boolean isHidden,
        List<String> tags,
        List<String> mediaUrls,
        int likesCount,
        LocalDateTime createdAt) {
}