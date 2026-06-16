package _Blog_Backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDto(
                long id,
                UserProfileDTO author,
                String title,
                String content,
                List<String> tag,
                List<String> mediaUrls,
                LocalDateTime createAt,
                LocalDateTime updatedAt) {

}
