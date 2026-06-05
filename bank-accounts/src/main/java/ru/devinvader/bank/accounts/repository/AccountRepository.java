package ru.devinvader.bank.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.accounts.model.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends CrudRepository<Account, UUID> {

    Optional<Account> findByLogin(String login);

    List<Account> findAllByLoginNot(String login);
}
