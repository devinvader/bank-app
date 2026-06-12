package ru.devinvader.bank.common.model;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.UUID;

public class NotificationMessages {

    private final MessageSource messageSource;

    public NotificationMessages(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String forDeposit(UUID accountId, BigDecimal amount) {
        return messageSource.getMessage("notification.deposit",
                new Object[]{amount, accountId}, LocaleContextHolder.getLocale());
    }

    public String forWithdrawal(UUID accountId, BigDecimal amount) {
        return messageSource.getMessage("notification.withdrawal",
                new Object[]{amount, accountId}, LocaleContextHolder.getLocale());
    }

    public String forProfileUpdate(UUID accountId) {
        return messageSource.getMessage("notification.profile-update",
                new Object[]{accountId}, LocaleContextHolder.getLocale());
    }

    public String forTransferSent(UUID toAccount, BigDecimal amount) {
        return messageSource.getMessage("notification.transfer-sent",
                new Object[]{amount, toAccount}, LocaleContextHolder.getLocale());
    }

    public String forTransferReceived(UUID fromAccount, BigDecimal amount) {
        return messageSource.getMessage("notification.transfer-received",
                new Object[]{amount, fromAccount}, LocaleContextHolder.getLocale());
    }
}
