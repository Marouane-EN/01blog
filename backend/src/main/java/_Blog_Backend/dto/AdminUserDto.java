package _Blog_Backend.dto;

import java.time.LocalDateTime;

public record AdminUserDto(
        Long id,
        String username,
        String email,
        String role,
        boolean isBlocked,
        boolean isActive,
        LocalDateTime createdAt) {
}