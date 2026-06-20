package _Blog_Backend.controller;

import java.util.concurrent.TimeUnit;

import org.hibernate.Hibernate;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.entity.PostMedia;
import _Blog_Backend.repository.PostMediaRepository;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.service.LocalFileStorageService;
import _Blog_Backend.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@RestController
@RequiredArgsConstructor
public class MediaController {

    private final LocalFileStorageService fileStorageService;
    private final PostMediaRepository postMediaRepository;
    private final UserRepository userRepository;
    private final RateLimitingService rateLimiter;

    @GetMapping("/uploads/posts/{filename}")
    public ResponseEntity<?> getPostImage(@PathVariable String filename, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        String dbUrl = "/uploads/posts/" + filename;

        PostMedia media = postMediaRepository.findByMediaUrl(dbUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));

        try {
            Hibernate.initialize(media.getPost());
        } catch (EntityNotFoundException e) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This image belongs to a banned or deleted post.");
        }

        Resource resource = fileStorageService.loadFileAsResource("uploads/posts/", filename);

        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (filename.toLowerCase().endsWith(".png"))
            mediaType = MediaType.IMAGE_PNG;
        else if (filename.toLowerCase().endsWith(".gif"))
            mediaType = MediaType.IMAGE_GIF;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    @GetMapping("/uploads/profiles/{filename}")
    public ResponseEntity<?> getProfileImage(@PathVariable String filename, HttpServletRequest httpRequest) {
        String ipAddress = rateLimiter.getClientIp(httpRequest);

        Bucket bucket = rateLimiter.resolveBucket(ipAddress);
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many attempts. Please try again in 15 minutes.");
        }

        String dbUrl = "/uploads/profiles/" + filename;

        userRepository.findByProfilePictureUrl(dbUrl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));

        Resource resource = fileStorageService.loadFileAsResource("uploads/profiles/", filename);

        CacheControl cacheControl = CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic();

        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }
}