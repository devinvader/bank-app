package ru.devinvader.bank.accounts.repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.accounts.model.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<Account, UUID> {

    @Query("SELECT * FROM accounts WHERE id != :id")
    List<Account> findAllByIdNot(UUID id);

    @Query("SELECT id FROM accounts WHERE id IN (:ids)")
    List<UUID> findExistingIds(Collection<UUID> ids);

    @Modifying
    @Query("UPDATE accounts SET balance = balance - :amount, updated_at = :updatedAt "
            + "WHERE id = :id AND balance >= :amount")
    int debit(UUID id, BigDecimal amount, Instant updatedAt);

    @Modifying
    @Query("UPDATE accounts SET balance = balance + :amount, updated_at = :updatedAt WHERE id = :id")
    int credit(UUID id, BigDecimal amount, Instant updatedAt);
}
