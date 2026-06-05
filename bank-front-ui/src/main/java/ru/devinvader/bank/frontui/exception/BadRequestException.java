package ru.devinvader.bank.frontui.exception;

public class BadRequestException extends BankApiException {
    public BadRequestException(String msg) { super(msg); }
}
