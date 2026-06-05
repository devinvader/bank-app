package ru.devinvader.bank.cash.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.cash.model.CashOperation;

import java.util.List;
import java.util.UUID;

@Repository
public interface CashRepository extends CrudRepository<CashOperation, UUID> {

    List<CashOperation> findByAccountId(String accountId);
}
