package ru.devinvader.bank.frontui.service;

import ru.devinvader.bank.frontui.model.AccountPageModel;
import ru.devinvader.bank.frontui.model.CashAction;
import java.math.BigDecimal;

public interface CashFrontService {
    AccountPageModel processCashOperation(BigDecimal amount, CashAction action);
}
