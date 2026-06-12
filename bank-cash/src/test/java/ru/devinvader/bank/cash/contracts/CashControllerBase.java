package ru.devinvader.bank.cash.contracts;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.devinvader.bank.cash.config.TestSecurityConfig;
import ru.devinvader.bank.cash.repository.CashRepository;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.model.AccountResponse;
import ru.devinvader.bank.commontest.config.AbstractTestcontainersConfiguration;
import ru.devinvader.bank.commontest.util.JwtTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({AbstractTestcontainersConfiguration.class, TestSecurityConfig.class})
@AutoConfigureStubRunner(
    ids = "ru.devinvader.bank.accounts:bank-accounts:+:stubs",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public abstract class CashControllerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CashRepository cashRepository;

    @Autowired
    private AccountsClient accountsClient;

    @Autowired
    protected StubFinder stubFinder;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        cashRepository.deleteAll();

        doNothing().when(accountsClient).credit(any(UUID.class), any(BigDecimal.class));
        doNothing().when(accountsClient).debit(any(UUID.class), any(BigDecimal.class));
        when(accountsClient.getAccount(eq(UUID.fromString(JwtTestUtils.TEST_SUBJECT))))
                .thenReturn(new AccountResponse(
                        UUID.fromString(JwtTestUtils.TEST_SUBJECT), "Test User", LocalDate.of(1990, 1, 1),
                        BigDecimal.valueOf(1000)));
    }
}
