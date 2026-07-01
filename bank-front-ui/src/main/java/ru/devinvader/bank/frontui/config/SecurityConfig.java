package ru.devinvader.bank.frontui.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.client.provider.keycloak.end-session-endpoint:http://localhost:8089/realms/bank/protocol/openid-connect/logout}")
    private String endSessionEndpoint;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id:bank-front-ui}")
    private String clientId;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/oauth2/authorization/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutSuccessHandler(this::keycloakLogout)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .build();
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
