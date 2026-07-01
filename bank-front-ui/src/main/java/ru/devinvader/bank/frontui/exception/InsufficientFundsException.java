package ru.devinvader.bank.frontui.exception;

public class InsufficientFundsException extends BankApiException {
    public InsufficientFundsException(String msg) { super(msg); }
}
