package ru.devinvader.bank.frontui.service;

import ru.devinvader.bank.frontui.model.AccountPageModel;
import java.math.BigDecimal;

public interface TransferFrontService {
    AccountPageModel processTransfer(String targetLogin, BigDecimal amount);
}
