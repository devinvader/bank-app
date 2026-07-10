package ru.devinvader.bank.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

import java.util.List;
import java.util.function.Consumer;

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
        retryWithLimit(repository.findByStatus(TransferStatus.FAILED),
                (t) -> transferService.retryTransfer(t), "failed");
        retryWithLimit(repository.findByStatus(TransferStatus.COMPENSATION_FAILED),
                (t) -> transferService.resumeTransfer(t), "failed transfer without debit");
    }

    private void retryWithLimit(List<TransferRecord> transfers, Consumer<TransferRecord> action, String label) {
        for (var transfer : transfers) {
            if (transfer.retryCount() >= maxRetries) {
                log.warn("Transfer {} exceeded max retries ({}), giving up",
                        transfer.id(), maxRetries);
                continue;
            }
            try {
                log.info("Retrying {} transfer: {} (attempt {}/{})",
                        label, transfer.id(), transfer.retryCount() + 1, maxRetries);
                action.accept(transfer);
            } catch (Exception e) {
                log.error("Retry failed for transfer: {}", transfer.id(), e);
            }
        }
    }
}
