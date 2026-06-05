package ru.devinvader.bank.frontui.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(UUID id, String fromLogin, String toLogin,
                                BigDecimal amount, String status, Instant timestamp) {}
