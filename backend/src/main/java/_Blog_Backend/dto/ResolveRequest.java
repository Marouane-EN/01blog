package _Blog_Backend.dto;

import _Blog_Backend.types.AdminAction;
import jakarta.validation.constraints.NotNull;

public record ResolveRequest(
        @NotNull(message = "You must provide an admin action (BAN_USER, HIDE_CONTENT, or DISMISS).") AdminAction action) {
}