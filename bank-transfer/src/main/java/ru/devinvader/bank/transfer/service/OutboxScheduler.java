package ru.devinvader.bank.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

@Slf4j
@Component
public class OutboxScheduler {

    private final TransferRepository repository;
    private final TransferService transferService;
    private final int maxRetries;

    public OutboxScheduler(TransferRepository repository,
                           TransferService transferService,
                           @Value("${outbox.scheduler.max-retries:3}") int maxRetries) {
        this.repository = repository;
        this.transferService = transferService;
        this.maxRetries = maxRetries;
    }

    @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
    public void processOutbox() {
        var failedTransfers = repository.findByStatus(TransferStatus.FAILED);

        for (var transfer : failedTransfers) {
            if (transfer.retryCount() >= maxRetries) {
                log.warn("Transfer {} exceeded max retries ({}), giving up",
                        transfer.id(), maxRetries);
                continue;
            }
            try {
                log.info("Retrying failed transfer: {} (attempt {}/{})",
                        transfer.id(), transfer.retryCount() + 1, maxRetries);
                transferService.retryTransfer(transfer);
            } catch (Exception e) {
                log.error("Retry failed for transfer: {}", transfer.id(), e);
            }
        }
    }
}
