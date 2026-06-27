package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDto(
        long id,
        String slug,
        UserDto author,
        String title,
        String content,
        List<String> tag,
        List<String> mediaUrls,
        int totalLikes,
        int totalComments,
        boolean likedByCurrentUser,
        LocalDateTime createAt,
        LocalDateTime updatedAt) {

}
