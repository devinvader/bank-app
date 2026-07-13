package ru.devinvader.bank.common.model;

import java.util.List;
import java.util.UUID;

public record AccountsExistenceResponse(List<UUID> missing) {}
