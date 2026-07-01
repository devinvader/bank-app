package ru.devinvader.bank.notifications.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.notifications.model.NotificationStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, UUID> {

    List<Notification> findByStatus(NotificationStatus status);
}
