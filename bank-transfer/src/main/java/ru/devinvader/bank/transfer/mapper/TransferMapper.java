package ru.devinvader.bank.transfer.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.model.TransferStatus;

import java.time.Instant;
import java.util.UUID;

@Component
public class TransferMapper {

    public TransferRecord toEntity(UUID fromAccountId, TransferRequest request) {
        return TransferRecord.builder()
                .fromAccount(fromAccountId)
                .toAccount(request.toAccountId())
                .amount(request.amount())
                .status(TransferStatus.PENDING)
                .createdAt(Instant.now())
                .newEntity(true)
                .retryCount(0)
                .build();
    }

    public TransferResponse toResponse(TransferRecord record) {
        return new TransferResponse(record.id(), record.fromAccount(), record.toAccount(),
                record.amount(), record.status(), record.createdAt());
    }
}
