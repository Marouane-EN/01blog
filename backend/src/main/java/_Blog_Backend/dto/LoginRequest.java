package _Blog_Backend.dto;

public record LoginRequest(
        String identifier,
        String password) {
}
