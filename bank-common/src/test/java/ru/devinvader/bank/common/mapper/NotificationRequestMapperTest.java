package ru.devinvader.bank.common.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.common.model.NotificationType;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRequestMapperTest {

    private static final UUID ACC_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ACC_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID ACC_3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    private final NotificationRequestMapper mapper = new NotificationRequestMapper();

    @Test
    void toRequest_shouldMapAllFields() {
        var request = mapper.toRequest(
                NotificationType.DEPOSIT, ACC_1, BigDecimal.valueOf(100), "Deposit");

        assertThat(request.type()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(request.accountId()).isEqualTo(ACC_1);
        assertThat(request.amount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(request.message()).isEqualTo("Deposit");
    }

    @Test
    void toRequest_withdrawal_shouldMapCorrectly() {
        var request = mapper.toRequest(
                NotificationType.WITHDRAWAL, ACC_2, BigDecimal.valueOf(200), "Withdrawal");

        assertThat(request.type()).isEqualTo(NotificationType.WITHDRAWAL);
        assertThat(request.accountId()).isEqualTo(ACC_2);
    }

    @Test
    void toRequest_profileUpdate_shouldMapZeroAmount() {
        var request = mapper.toRequest(
                NotificationType.PROFILE_UPDATE, ACC_3, BigDecimal.ZERO, "Profile updated");

        assertThat(request.type()).isEqualTo(NotificationType.PROFILE_UPDATE);
        assertThat(request.amount()).isEqualTo(BigDecimal.ZERO);
    }
}
