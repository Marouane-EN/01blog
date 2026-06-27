package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record PostSearchDto(
        String author,
        String title,
        LocalDateTime createAt) {

}
