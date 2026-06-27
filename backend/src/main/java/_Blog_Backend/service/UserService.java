package _Blog_Backend.service;

import java.io.IOException;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.UserDto;
import _Blog_Backend.dto.CursorResponse;
import _Blog_Backend.dto.PublicProfileDto;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public String updateAvatar(MultipartFile file, User currentUser) throws IOException {

        if (currentUser.getProfilePictureUrl() != null) {
            fileUploadService.deleteFileByUrl(currentUser.getProfilePictureUrl());
        }

        String secureUrl = fileUploadService.uploadFile(file);

        currentUser.setProfilePictureUrl(secureUrl);
        userRepository.save(currentUser);

        return currentUser.getProfilePictureUrl();
    }

    @Transactional(readOnly = true)
    public PublicProfileDto getPublicProfile(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isBlocked() || !user.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return new PublicProfileDto(
                user.getId(),
                user.getUsername(),
                user.getBio(),
                user.getProfilePictureUrl(),
                user.getPostsCount(),
                user.getFollowersCount(),
                user.getFollowingCount(),
                user.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public CursorResponse<UserDto> searchUsers(String keyword, Long cursor) {

        Pageable pageRequest = PageRequest.of(0, 11);
        List<User> users;

        if (cursor == null) {
            users = userRepository.searchPublicUsers(keyword, pageRequest);
        } else {
            users = userRepository.searchPublicUsersByCursor(keyword, cursor, pageRequest);
        }

        boolean hasMore = users.size() > 10;
        Long nextCursor = null;

        if (hasMore) {
            users.remove(users.size() - 1);
            nextCursor = users.get(users.size() - 1).getId();
        }

        List<UserDto> cleanUsers = users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getProfilePictureUrl()))
                .toList();

        return new CursorResponse<>(cleanUsers, nextCursor, hasMore);
    }
}
