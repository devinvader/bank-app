package ru.devinvader.bank.frontui.exception;

public class UnauthorizedException extends BankApiException {
    public UnauthorizedException(String msg) { super(msg); }
}
