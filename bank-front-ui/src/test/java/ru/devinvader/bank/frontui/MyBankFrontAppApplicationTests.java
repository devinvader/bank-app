package ru.devinvader.bank.frontui;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.devinvader.bank.frontui.integration.BaseIntegrationTest;
import ru.devinvader.bank.frontui.service.AccountFrontService;

import static org.assertj.core.api.Assertions.assertThat;

class MyBankFrontAppApplicationTests extends BaseIntegrationTest {

    @MockitoBean
    private AccountFrontService accountFrontService;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }
}
