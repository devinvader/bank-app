package ru.devinvader.bank.accounts.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.accounts.integration.TestcontainersConfiguration;
import ru.devinvader.bank.accounts.repository.AccountRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AccountControllerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        accountRepository.deleteAll();
        jdbcTemplate.update(
                "INSERT INTO accounts (id, login, name, birthdate, balance, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(),
                "user1",
                "Иван Иванов",
                java.sql.Date.valueOf("1990-01-01"),
                new java.math.BigDecimal("1000.00"),
                Timestamp.from(Instant.now())
        );

        when(jwtDecoder.decode(anyString())).thenReturn(
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .claim("scope", "accounts:read accounts:write")
                        .claim("preferred_username", "user1")
                        .claim("sub", "user1")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build()
        );
    }
}


