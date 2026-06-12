package ru.devinvader.bank.frontui.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.frontui.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FrontUiMapperTest {

    private final FrontUiMapper mapper = new FrontUiMapper();

    private static final UUID ACC_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACC_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID TARGET_ACC = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Test
    void toAccountDto_shouldExtractIdAndName() {
        var response = new AccountResponse(ACC_1, "Test User",
                LocalDate.of(1990, 1, 1), BigDecimal.valueOf(1000));

        var dto = mapper.toAccountDto(response);

        assertThat(dto.accountId()).isEqualTo(ACC_1);
        assertThat(dto.name()).isEqualTo("Test User");
    }

    @Test
    void toAccountPageModel_shouldMapAllFields() {
        var account = new AccountResponse(ACC_1, "Test User",
                LocalDate.of(1990, 6, 15), BigDecimal.valueOf(2500));
        var targets = List.of(new AccountDto(ACC_2, "Target"));

        var model = mapper.toAccountPageModel(account, targets);

        assertThat(model.name()).isEqualTo("Test User");
        assertThat(model.birthdate()).isEqualTo(LocalDate.of(1990, 6, 15));
        assertThat(model.balance()).isEqualTo(BigDecimal.valueOf(2500));
        assertThat(model.transferTargets()).hasSize(1);
        assertThat(model.transferTargets().getFirst().accountId()).isEqualTo(ACC_2);
        assertThat(model.errors()).isNull();
        assertThat(model.info()).isNull();
    }

    @Test
    void toAccountPageModel_withEmptyTargets_shouldMapCorrectly() {
        var account = new AccountResponse(ACC_1, "User",
                LocalDate.of(2000, 1, 1), BigDecimal.ZERO);

        var model = mapper.toAccountPageModel(account, List.of());

        assertThat(model.transferTargets()).isEmpty();
        assertThat(model.balance()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void toAccountUpdateRequest_shouldMapFields() {
        var request = mapper.toAccountUpdateRequest("New Name", LocalDate.of(1995, 3, 20));

        assertThat(request.name()).isEqualTo("New Name");
        assertThat(request.birthdate()).isEqualTo(LocalDate.of(1995, 3, 20));
    }

    @Test
    void toTransferRequest_shouldMapFields() {
        var request = mapper.toTransferRequest(TARGET_ACC, BigDecimal.valueOf(500));

        assertThat(request.toAccountId()).isEqualTo(TARGET_ACC);
        assertThat(request.amount()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    void toCashRequest_shouldWrapAmount() {
        var request = mapper.toCashRequest(BigDecimal.valueOf(100));

        assertThat(request.amount()).isEqualTo(BigDecimal.valueOf(100));
    }
}
