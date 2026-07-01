package ru.devinvader.bank.notifications.mapper;

import org.junit.jupiter.api.Test;
import ru.devinvader.bank.common.model.NotificationRequest;
import ru.devinvader.bank.common.model.NotificationType;
import ru.devinvader.bank.notifications.model.NotificationStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        var accountId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var request = new NotificationRequest(NotificationType.DEPOSIT, accountId,
                BigDecimal.valueOf(500), "Deposit of 500");

        var entity = mapper.toEntity(request);

        assertThat(entity.type()).isEqualTo(NotificationType.DEPOSIT);
        assertThat(entity.accountId()).isEqualTo(accountId);
        assertThat(entity.amount()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(entity.message()).isEqualTo("Deposit of 500");
        assertThat(entity.status()).isEqualTo(NotificationStatus.PENDING);
        assertThat(entity.newEntity()).isTrue();
        assertThat(entity.createdAt()).isNotNull();
    }

    @Test
    void toEntity_withTransferType_shouldMapCorrectly() {
        var accountId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var request = new NotificationRequest(NotificationType.TRANSFER, accountId,
                BigDecimal.valueOf(1000), "Transfer sent");

        var entity = mapper.toEntity(request);

        assertThat(entity.type()).isEqualTo(NotificationType.TRANSFER);
        assertThat(entity.accountId()).isEqualTo(accountId);
        assertThat(entity.retryCount()).isZero();
    }

    @Test
    void toEntity_withZeroAmount_shouldMapCorrectly() {
        var request = new NotificationRequest(NotificationType.PROFILE_UPDATE, UUID.fromString("00000000-0000-0000-0000-000000000003"),
                BigDecimal.ZERO, "Profile updated");

        var entity = mapper.toEntity(request);

        assertThat(entity.amount()).isEqualTo(BigDecimal.ZERO);
        assertThat(entity.type()).isEqualTo(NotificationType.PROFILE_UPDATE);
    }
}
