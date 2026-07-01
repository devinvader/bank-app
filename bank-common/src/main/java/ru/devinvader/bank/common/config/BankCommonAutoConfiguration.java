package ru.devinvader.bank.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import ru.devinvader.bank.common.model.NotificationMessages;

@AutoConfiguration
@ComponentScan("ru.devinvader.bank.common")
public class BankCommonAutoConfiguration {

    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    @Bean
    public MessageSource notificationMessageSource() {
        var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:notification-messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public NotificationMessages notificationMessages(
            @Qualifier("notificationMessageSource") MessageSource messageSource) {
        return new NotificationMessages(messageSource);
    }
}
