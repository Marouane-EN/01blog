package _Blog_Backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class DatabaseSeeder {
    @Value("${application.admin.username}")
    private String adminUsername;

    @Value("${application.admin.password}")
    private String adminPassword;

    @Value("${application.admin.email}")
    private String adminEmail;

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role("ADMIN")
                        .build();

                userRepository.save(admin);
                log.info("✅ Super Admin created successfully with email: {}", adminEmail);
            }
        };
    }
}