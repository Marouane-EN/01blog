package _Blog_Backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @NotBlank(message = "You must provide a reason for this report.")
        @Size(min = 10, max = 500, message = "Report reason must be between 10 and 500 characters.")
        String reason) {
}