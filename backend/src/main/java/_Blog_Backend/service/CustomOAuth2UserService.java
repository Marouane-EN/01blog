package _Blog_Backend.service;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        String email = oAuth2User.getAttribute("email");

        String username = oAuth2User.getAttribute("login");

        if (username == null) {
            username = oAuth2User.getAttribute("name");
        }
        
        String providerId = oAuth2User.getName();

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty() && email != null) {
            User newUser = User.builder()
                    .username(username.replaceAll("//s+", "").toLowerCase())
                    .email(email)
                    .authProvider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(newUser);
        }

        return oAuth2User;
    }

}
