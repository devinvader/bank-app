package ru.devinvader.bank.cash.service;

import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;

import java.util.UUID;

public interface CashService {

    CashResponse deposit(UUID accountId, CashRequest request);

    CashResponse withdraw(UUID accountId, CashRequest request);
}
