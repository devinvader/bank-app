package ru.devinvader.bank.frontui.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public record AccountUpdateRequestDto(
        @NotBlank String name,
        @NotNull @Past LocalDate birthdate
) {}
