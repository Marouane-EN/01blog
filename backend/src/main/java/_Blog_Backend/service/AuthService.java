package _Blog_Backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User RegisterLocalUser(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already in use!");
        }
        String hashedPassword = passwordEncoder.encode(password);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash(hashedPassword)
                .build();

        return userRepository.save(newUser);
    }
}
