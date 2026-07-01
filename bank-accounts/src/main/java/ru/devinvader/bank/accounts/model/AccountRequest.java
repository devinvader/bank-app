package ru.devinvader.bank.accounts.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record AccountRequest(
        @NotBlank String name,
        @NotNull @Past LocalDate birthdate
) {}
