package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record PostSearchDto(
        Long id,
        UserDto author,
        String title,
        String slug,
        LocalDateTime createAt) {

}
