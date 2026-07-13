package ru.devinvader.bank.accounts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/accounts/{accountId}/credit", "/api/accounts/{accountId}/debit")
                        .hasAuthority("SCOPE_accounts:write")
                        .requestMatchers(HttpMethod.PUT, "/api/accounts/me")
                        .hasAuthority("SCOPE_accounts:write")
                        .requestMatchers(HttpMethod.POST, "/api/accounts/exists")
                        .hasAuthority("SCOPE_accounts:read")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/accounts/me", "/api/accounts/{accountId}", "/api/accounts/transfer-targets")
                        .hasAuthority("SCOPE_accounts:read")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}
