package _Blog_Backend.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("login");
        String providerId = oAuth2User.getName();

        String profilePictureUrl = null;
        if ("GITHUB".equals(provider)) {
            profilePictureUrl = oAuth2User.getAttribute("avatar_url");
            email = email + "+github.com"; // Append domain to email for uniqueness
        } else if ("GOOGLE".equals(provider)) {
            profilePictureUrl = oAuth2User.getAttribute("picture");
            email = email + "+google.com"; // Append domain to email for uniqueness
        }

        if (username == null) {
            username = oAuth2User.getAttribute("name");
        }

        if (email == null) {
            email = providerId + "+" + username + "@users.noreply.github.com";
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        String cleanUsername = username.replaceAll("\\s+", "").toLowerCase(); // Fixed regex for whitespace

        if (existingUser.isEmpty()) {
            String uniqueUsername = generateUniqueUsername(cleanUsername);

            User newUser = User.builder()
                    .username(uniqueUsername)
                    .email(email)
                    .authProvider(provider)
                    .providerId(providerId)
                    .profilePictureUrl(profilePictureUrl)
                    .build();
            userRepository.save(newUser);
        }

        return oAuth2User;
    }

    private String generateUniqueUsername(String username) {
        String finalUsername = username;
        int suffix = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + suffix;
            suffix++;
        }
        return finalUsername;
    }
}