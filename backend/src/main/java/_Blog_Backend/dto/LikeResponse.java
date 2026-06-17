package _Blog_Backend.dto;

public record LikeResponse(
        boolean isLiked,
        long totalLikes) {
}