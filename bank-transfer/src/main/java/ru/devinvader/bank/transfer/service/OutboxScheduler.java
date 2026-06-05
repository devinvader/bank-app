package ru.devinvader.bank.transfer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferStatus;
import ru.devinvader.bank.transfer.repository.TransferRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final TransferRepository repository;
    private final TransferService transferService;

    @Scheduled(fixedDelayString = "${outbox.scheduler.interval:5000}")
    @Transactional
    public void processOutbox() {
        var failedTransfers = repository.findByStatus(TransferStatus.FAILED);

        for (TransferRecord transfer : failedTransfers) {
            try {
                log.info("Retrying failed transfer: {}", transfer.id());
                transfer = transfer.toBuilder()
                        .status(TransferStatus.PENDING)
                        .build();
                repository.save(transfer);

                var request = new ru.devinvader.bank.transfer.model.TransferRequest(
                        transfer.toAccount(), transfer.amount());
                transferService.execute(transfer.fromAccount(), request);
            } catch (Exception e) {
                log.error("Retry failed for transfer: {}", transfer.id(), e);
            }
        }
    }
}
