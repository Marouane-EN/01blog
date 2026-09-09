package _Blog_Backend.dto;

public record PopularUserDto(Long id, String username, String profilePictureUrl, long followerCount) {
}
