package ru.devinvader.bank.accounts.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal requested, BigDecimal available) {
        super("Insufficient balance: requested " + requested + ", available " + available);
    }
}
