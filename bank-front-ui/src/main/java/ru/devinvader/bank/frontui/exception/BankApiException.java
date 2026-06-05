package ru.devinvader.bank.frontui.exception;

public abstract class BankApiException extends RuntimeException {
    protected BankApiException(String msg) { super(msg); }
    protected BankApiException(String msg, Throwable cause) { super(msg, cause); }
}
