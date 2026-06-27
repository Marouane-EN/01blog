package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record PublicProfileDto(
        Long id,
        String username,
        String bio,
        String profilePictureUrl,
        int postsCount,
        int followersCount,
        int followingCount,
        LocalDateTime joinedAt) {
}