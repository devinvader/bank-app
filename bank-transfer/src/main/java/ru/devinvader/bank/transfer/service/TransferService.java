package ru.devinvader.bank.transfer.service;

import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;

import java.util.List;

public interface TransferService {

    TransferResponse execute(String fromLogin, TransferRequest request);

    List<TransferResponse> getHistory(String login);
}
