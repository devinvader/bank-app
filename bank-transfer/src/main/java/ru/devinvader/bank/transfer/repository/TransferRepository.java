package ru.devinvader.bank.transfer.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.transfer.model.TransferRecord;
import ru.devinvader.bank.transfer.model.TransferStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferRepository extends CrudRepository<TransferRecord, UUID> {

    List<TransferRecord> findByStatus(TransferStatus status);

    List<TransferRecord> findByFromAccount(String login);
}
