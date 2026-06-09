package _Blog_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "Title is required") @Size(min = 1, max = 128, message = "Title must be between 1 and 128 characters") String title,
        @NotBlank(message = "Content is required") @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters") String content,
        @Size(max = 30, message = "Tag must be between 1 and 30 characters") String tag

) {

}
