package ru.devinvader.bank.transfer.service;

import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;

import java.util.List;
import java.util.UUID;

public interface TransferService {

    TransferResponse execute(UUID fromAccountId, TransferRequest request);

    TransferResponse retryTransfer(TransferRecord existing);

    TransferResponse resumeTransfer(TransferRecord existing);

    List<TransferResponse> getHistory(UUID accountId);
}
