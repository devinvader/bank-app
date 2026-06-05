package ru.devinvader.bank.cash.service;

import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashResponse;

public interface CashService {

    CashResponse deposit(String login, CashRequest request);

    CashResponse withdraw(String login, CashRequest request);
}
