package ru.devinvader.bank.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.common.client.AccountsClient;
import ru.devinvader.bank.common.client.NotificationClient;
import ru.devinvader.bank.common.exception.InsufficientBalanceException;
import ru.devinvader.bank.common.model.NotificationMessages;
import ru.devinvader.bank.common.model.NotificationType;
import ru.devinvader.bank.transfer.mapper.TransferMapper;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TransferServiceImpl implements TransferService {
    private final TransferRepository repository;
    private final AccountsClient accountsClient;
    private final NotificationClient notificationClient;
    private final NotificationMessages notificationMessages;
    private final TransferMapper transferMapper;

    public TransferServiceImpl(TransferRepository repository,
                               AccountsClient accountsClient,
                               NotificationClient notificationClient,
                               NotificationMessages notificationMessages,
                               TransferMapper transferMapper) {
        this.repository = repository;
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.notificationMessages = notificationMessages;
        this.transferMapper = transferMapper;
    }

    @Override
    @Transactional
    public TransferResponse execute(UUID fromAccountId, TransferRequest request) {
        var transfer = transferMapper.toEntity(fromAccountId, request);
        transfer = repository.save(transfer);
        return processTransfer(transfer, false);
    }

    @Override
    @Transactional
    public TransferResponse retryTransfer(TransferRecord existing) {
        var transfer = existing.toBuilder()
                .status(TransferStatus.PENDING)
                .retryCount(existing.retryCount() + 1)
                .build();
        transfer = repository.save(transfer);
        return processTransfer(transfer, false);
    }

    @Override
    @Transactional
    public TransferResponse resumeTransfer(TransferRecord existing) {
        var transfer = existing.toBuilder()
                .status(TransferStatus.PENDING)
                .retryCount(existing.retryCount() + 1)
                .build();
        transfer = repository.save(transfer);
        return processTransfer(transfer, true);
    }

    private TransferResponse processTransfer(TransferRecord transfer, boolean skipDebit) {
        var debitSucceeded = skipDebit;
        try {
            if (!skipDebit) {
                debit(transfer);
                debitSucceeded = true;
            }
            credit(transfer);

            transfer = transfer.toBuilder()
                    .status(TransferStatus.COMPLETED)
                    .completedAt(Instant.now())
                    .build();
            transfer = repository.save(transfer);

            notifyParticipants(transfer);

            return transferMapper.toResponse(transfer);
        } catch (InsufficientBalanceException e) {
            transfer = transfer.toBuilder()
                    .status(TransferStatus.REJECTED)
                    .completedAt(Instant.now())
                    .build();
            repository.save(transfer);
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed: {}", e.getMessage());
            var compensated = false;
            if (debitSucceeded) {
                compensated = compensate(transfer);
            }

            var status = debitSucceeded && !compensated
                    ? TransferStatus.COMPENSATION_FAILED
                    : TransferStatus.FAILED;
            transfer = transfer.toBuilder()
                    .status(status)
                    .completedAt(Instant.now())
                    .build();
            repository.save(transfer);

            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    private void debit(TransferRecord transfer) {
        accountsClient.debit(transfer.fromAccount(), transfer.amount());
    }

    private void credit(TransferRecord transfer) {
        accountsClient.credit(transfer.toAccount(), transfer.amount());
    }

    private boolean compensate(TransferRecord transfer) {
        try {
            accountsClient.credit(transfer.fromAccount(), transfer.amount());
            log.info("Compensation: credited back {} to {}", transfer.amount(), transfer.fromAccount());
            return true;
        } catch (Exception e) {
            log.error("Compensation failed: {}", e.getMessage());
            return false;
        }
    }

    private void notifyParticipants(TransferRecord transfer) {
        try {
            notificationClient.send(NotificationType.TRANSFER, transfer.fromAccount(), transfer.amount(),
                    notificationMessages.forTransferSent(transfer.toAccount(), transfer.amount()));
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
        try {
            notificationClient.send(NotificationType.TRANSFER, transfer.toAccount(), transfer.amount(),
                    notificationMessages.forTransferReceived(transfer.fromAccount(), transfer.amount()));
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    @Override
    public List<TransferResponse> getHistory(UUID accountId) {
        return repository.findByFromAccount(accountId).stream()
                .map(transferMapper::toResponse)
                .toList();
    }
}
