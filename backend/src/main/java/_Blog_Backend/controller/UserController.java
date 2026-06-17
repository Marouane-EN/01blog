package _Blog_Backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import _Blog_Backend.dto.SubscriptionResponse;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.service.LocalFileStorageService;
import _Blog_Backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final LocalFileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @PostMapping("/me/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        try {
            String imageUrl = fileStorageService.saveProfilePicture(file);
            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED).body("Profile picture updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }

    @PostMapping("/{username}/subscribe")
    public ResponseEntity<?> subscribeToUser(
            @PathVariable String username,
            @AuthenticationPrincipal User currentUser) {
        SubscriptionResponse response = subscriptionService.toggleSubscription(username, currentUser);
        return ResponseEntity.ok(response);
    }
}