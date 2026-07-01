package ru.devinvader.bank.accounts.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.accounts.model.Account;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<Account, UUID> {

    @Query("SELECT * FROM accounts WHERE id != :id")
    List<Account> findAllByIdNot(UUID id);
}
