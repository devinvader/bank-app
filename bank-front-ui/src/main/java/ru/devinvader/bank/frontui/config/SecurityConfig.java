package ru.devinvader.bank.frontui.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.end-session-endpoint:http://localhost:8089/realms/bank/protocol/openid-connect/logout}")
    private String endSessionEndpoint;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id:bank-front-ui}")
    private String clientId;

    @Bean
    @Primary
    public OidcUserService oidcUserService() {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) throws org.springframework.security.oauth2.core.OAuth2AuthenticationException {
                var idToken = userRequest.getIdToken();
                Set<SimpleGrantedAuthority> authorities = new HashSet<>(userRequest.getAccessToken().getScopes().stream()
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                        .toList());
                return new DefaultOidcUser(authorities, idToken);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**", "/error", "/oauth2/authorization/**", "/login-error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2SuccessHandler())
                        .failureHandler(oauth2FailureHandler())
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(this::keycloakLogout)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, auth) -> {
            response.sendRedirect("/account");
        };
    }

    @Bean
    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, exception) -> {
            request.getSession().setAttribute("loginError", exception.getMessage());
            response.sendRedirect("/login-error");
        };
    }

    private void keycloakLogout(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException {
        String baseUrl = request.getRequestURL().toString()
                .replace(request.getRequestURI(), "") + request.getContextPath();
        String redirectUrl = endSessionEndpoint
                + "?post_logout_redirect_uri="
                + URLEncoder.encode(baseUrl, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
