package _Blog_Backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import _Blog_Backend.dto.RegisterRequest;
import _Blog_Backend.dto.UserProfileDTO;
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

    @Transactional
    public UserProfileDTO registerLocalUser(RegisterRequest request) {
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

        userRepository.save(newUser);
        return mapToDto(newUser);
    }

    @Transactional(readOnly = true)
    public String loginLocalUser(String identifier, String password) {

        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, password));

        } catch (LockedException | DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This account has been suspended by an Administrator.");

        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        User authenticatedUser = (User) authentication.getPrincipal();

        return jwtService.generateToken(authenticatedUser);
    }

    private UserProfileDTO mapToDto(User user) {
        return new UserProfileDTO(user.getId(), user.getUsername(), user.getProfilePictureUrl(), user.getBio(),
                user.getFollowers().size(), user.getFollowing().size(), false);
    }
}
