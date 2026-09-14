package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String senderUsername,
        String senderProfilePictureUrl,
        String type,
        Long postId,
        String postSlug,
        boolean isRead,
        LocalDateTime createdAt) {
}