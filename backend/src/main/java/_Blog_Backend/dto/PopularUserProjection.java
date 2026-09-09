package _Blog_Backend.dto;

public interface PopularUserProjection {
    Long getId();

    String getUsername();

    String getProfilePictureUrl();

    long getFollowerCount();
}
