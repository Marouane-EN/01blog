package _Blog_Backend.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import _Blog_Backend.entity.User;
import _Blog_Backend.repository.UserRepository;
import _Blog_Backend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // This is the URL of your Angular Application!
    private final String FRONTEND_REDIRECT_URL = "http://localhost:4200/oauth2/redirect";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()
                .toUpperCase();

        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getName();
        String login = oAuth2User.getAttribute("login");
        if (email == null) {
            email = providerId + "+" + login + "@users.noreply.github.com";
        }
        if ("GITHUB".equals(provider)) {
            email = email + "+github.com"; // Append domain to email for uniqueness
        } else if ("GOOGLE".equals(provider)) {
            email = email + "+google.com"; // Append domain to email for uniqueness
        }
        User dbUser = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found after OAuth2 login"));

        String token = jwtService.generateToken(dbUser);

        // Build the redirect URL with the token attached as a query parameter
        String targetUrl = UriComponentsBuilder.fromUriString(FRONTEND_REDIRECT_URL)
                .queryParam("token", token)
                .build().toUriString();

        // Send the user's browser back to Angular!
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}