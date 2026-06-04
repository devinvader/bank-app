package ru.devinvader.bank.notifications.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.devinvader.bank.notifications.config.TestSecurityConfig;
import ru.devinvader.bank.notifications.repository.NotificationRepository;

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
    protected NotificationRepository notificationRepository;
}
