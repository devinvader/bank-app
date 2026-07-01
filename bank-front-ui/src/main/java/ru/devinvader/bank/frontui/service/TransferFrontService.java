package ru.devinvader.bank.frontui.service;

import ru.devinvader.bank.frontui.model.AccountPageModel;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransferFrontService {
    AccountPageModel processTransfer(UUID targetAccountId, BigDecimal amount);
}
