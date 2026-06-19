package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDto(
                long id,
                AuthorDto author,
                String title,
                String content,
                List<String> tag,
                List<String> mediaUrls,
                int totalLikes,
                boolean likedByCurrentUser,
                LocalDateTime createAt,
                LocalDateTime updatedAt) {

}
