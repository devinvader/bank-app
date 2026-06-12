package ru.devinvader.bank.cash.mapper;

import org.springframework.stereotype.Component;
import ru.devinvader.bank.cash.model.CashOperation;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class CashMapper {

    public CashOperation toEntity(UUID accountId, CashRequest request, CashOperationType type) {
        return CashOperation.builder()
                .accountId(accountId)
                .type(type)
                .amount(request.amount())
                .createdAt(Instant.now())
                .newEntity(true)
                .build();
    }

    public CashResponse toResponse(UUID accountId, BigDecimal balance,
                                   CashOperationType type, BigDecimal amount) {
        return new CashResponse(accountId, balance, type, amount);
    }
}
