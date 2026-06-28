package _Blog_Backend.dto;

import java.time.LocalDateTime;

public interface AdminPostProjection {
    Long getId();

    String getTitle();

    String getAuthorUsername();

    String getAuthorProfilePictureUrl();

    boolean getIsHidden();

    LocalDateTime getCreatedAt();

    int getLikesCount();

    int getCommentsCount();
}