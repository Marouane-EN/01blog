package _Blog_Backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _Blog_Backend.dto.AuthResponse;
import _Blog_Backend.dto.LoginRequest;
import _Blog_Backend.dto.RegisterRequest;
import _Blog_Backend.dto.UserProfileDTO;
import _Blog_Backend.entity.User;
import _Blog_Backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserProfileDTO> register(@RequestBody RegisterRequest request) {
        User savedUser = authService.RegisterLocalUser(request.username(), request.email(), request.password());
        return ResponseEntity
                .ok(new UserProfileDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = authService.loginLocalUser(request.identifier(), request.password());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
