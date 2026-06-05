package ru.devinvader.bank.frontui.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountData(String name, LocalDate birthdate, BigDecimal balance) {}
