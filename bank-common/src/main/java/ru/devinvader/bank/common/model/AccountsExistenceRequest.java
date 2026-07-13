package ru.devinvader.bank.common.model;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record AccountsExistenceRequest(@NotEmpty List<UUID> accountIds) {}
