package _Blog_Backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.RegisterRequest;
import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public User registerLocalUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: Email is already in use!");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(hashedPassword)
                .bio(request.bio())
                .birthDate(request.birthDate())
                .role("USER")
                .authProvider("LOCAL")
                .build();

        return userRepository.save(newUser);
    }

    public String loginLocalUser(String identifier, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, password));

        User authenticatedUser = (User) authentication.getPrincipal();

        return jwtService.generateToken(authenticatedUser);
    }
}
