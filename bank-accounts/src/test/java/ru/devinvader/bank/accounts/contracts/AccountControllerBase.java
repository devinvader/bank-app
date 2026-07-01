package ru.devinvader.bank.accounts.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.accounts.config.TestSecurityConfig;
import ru.devinvader.bank.accounts.repository.AccountRepository;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;
import ru.devinvader.bank.commontest.util.JwtTestUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({AbstractTestcontainersConfiguration.class, TestSecurityConfig.class})
public abstract class AccountControllerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        accountRepository.deleteAll();
        jdbcTemplate.update(
                "INSERT INTO accounts (id, name, birthdate, balance, created_at) VALUES (?, ?, ?, ?, ?)",
                UUID.fromString(JwtTestUtils.TEST_SUBJECT),
                "Иван Иванов",
                java.sql.Date.valueOf("1990-01-01"),
                new java.math.BigDecimal("1000.00"),
                Timestamp.from(Instant.now())
        );
    }
}
