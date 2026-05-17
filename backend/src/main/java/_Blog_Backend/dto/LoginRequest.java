package _Blog_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
                @NotBlank(message = "Identifier is required") @Size(min = 3, max = 50, message = "Identifier must be between 3 and 50 characters") String identifier,

                @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
}
