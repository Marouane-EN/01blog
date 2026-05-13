package _Blog_Backend.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            String providerId = oAuth2User.getName();
            String login = oAuth2User.getAttribute("login");
            email = providerId + "+" + login + "@users.noreply.github.com";
        }

        User dbUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));

        String token = jwtService.generateToken(dbUser);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\n  \"message\": \"Login Successful! Copy your token below.\",\n  \"token\": \"" + token + "\"\n}");
    }
}
