package ru.devinvader.bank.notifications.service;

import ru.devinvader.bank.notifications.model.Notification;
import ru.devinvader.bank.common.model.NotificationRequest;

public interface NotificationService {

    Notification send(NotificationRequest request);
}
