package ru.devinvader.bank.cash.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.devinvader.bank.cash.config.TestSecurityConfig;
import ru.devinvader.bank.cash.repository.CashRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
@Import({TestSecurityConfig.class, TestcontainersConfiguration.class})
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:/db/changelog/db.changelog-master.yaml"
})
public abstract class BaseIntegrationTest {

    @Autowired
    protected CashRepository cashRepository;

    @MockitoBean
    protected ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    protected OAuth2AuthorizedClientService authorizedClientService;
}
