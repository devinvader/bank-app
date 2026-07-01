package ru.devinvader.bank.cash.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.cash.model.CashOperationType;
import ru.devinvader.bank.cash.model.CashRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CashMapperTest {

    private static final UUID ACCOUNT_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACCOUNT_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final CashMapper mapper = new CashMapper();

    @Test
    void toEntity_shouldMapDepositRequestToEntity() {
        var request = new CashRequest(BigDecimal.valueOf(500));
        var entity = mapper.toEntity(ACCOUNT_ID_1, request, CashOperationType.DEPOSIT);

        assertThat(entity.accountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(entity.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(entity.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(entity.newEntity()).isTrue();
        assertThat(entity.createdAt()).isNotNull();
    }

    @Test
    void toEntity_shouldMapWithdrawalRequestToEntity() {
        var request = new CashRequest(BigDecimal.valueOf(200));
        var entity = mapper.toEntity(ACCOUNT_ID_2, request, CashOperationType.WITHDRAWAL);

        assertThat(entity.type()).isEqualTo(CashOperationType.WITHDRAWAL);
        assertThat(entity.amount()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(entity.accountId()).isEqualTo(ACCOUNT_ID_2);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        var response = mapper.toResponse(ACCOUNT_ID_1, BigDecimal.valueOf(1500),
                CashOperationType.DEPOSIT, BigDecimal.valueOf(500));

        assertThat(response.accountId()).isEqualTo(ACCOUNT_ID_1);
        assertThat(response.newBalance()).isEqualTo(BigDecimal.valueOf(1500));
        assertThat(response.type()).isEqualTo(CashOperationType.DEPOSIT);
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void toResponse_withdrawal_shouldMapCorrectType() {
        var response = mapper.toResponse(ACCOUNT_ID_1, BigDecimal.valueOf(800),
                CashOperationType.WITHDRAWAL, BigDecimal.valueOf(200));

        assertThat(response.type()).isEqualTo(CashOperationType.WITHDRAWAL);
        assertThat(response.newBalance()).isEqualTo(BigDecimal.valueOf(800));
    }
}
