package ru.devinvader.bank.frontui.exception;

public class ServiceUnavailableException extends BankApiException {
    public ServiceUnavailableException(String msg) { super(msg); }
    public ServiceUnavailableException(String msg, Throwable cause) { super(msg, cause); }
}
