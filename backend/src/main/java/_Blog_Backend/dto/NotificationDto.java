package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        String senderUsername,
        String senderProfilePictureUrl,
        String type,
        Long postId,
        boolean isRead,
        LocalDateTime createdAt) {
}