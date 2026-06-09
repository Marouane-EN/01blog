package _Blog_Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final LocalFileStorageService fileStorageService;
    private final UserRepository userRepository;

    @PostMapping("/me/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        try {
            String imageUrl = fileStorageService.saveProfilePicture(file);
            user.setProfilePictureUrl(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok("Profile picture updated successfully! URL: " + imageUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload image: " + e.getMessage());
        }
    }
}