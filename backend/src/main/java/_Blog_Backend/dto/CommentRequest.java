package _Blog_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
                @NotBlank(message = "Comment content cannot be blank")
                @Size(max = 200, message = "Comment content must not exceed 200 characters") String content,
                Long parentId) {
}
