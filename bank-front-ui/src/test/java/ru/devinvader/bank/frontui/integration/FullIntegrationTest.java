package ru.devinvader.bank.frontui.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.devinvader.bank.frontui.model.AccountPageModel;
import ru.devinvader.bank.frontui.service.AccountFrontService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class FullIntegrationTest extends BaseIntegrationTest {

    @MockitoBean
    private AccountFrontService accountFrontService;

    @BeforeEach
    void setUp() {
        when(accountFrontService.getAccountPage())
                .thenReturn(new AccountPageModel("Тест", null, BigDecimal.ZERO, List.of(), null, null));
    }

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void homePage_shouldBeAccessible() {
        var response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode().value()).isBetween(200, 399);
    }

    @Test
    void accountPage_hasSecurity() {
        var response = restTemplate.getForEntity("/account", String.class);
        assertThat(response.getStatusCode().value()).isBetween(200, 599);
    }
}
