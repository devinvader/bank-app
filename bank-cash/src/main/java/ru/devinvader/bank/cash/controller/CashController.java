package ru.devinvader.bank.cash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;
import ru.devinvader.bank.cash.service.CashService;
import ru.devinvader.bank.common.security.CurrentUser;

import java.util.UUID;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/deposit")
    public ResponseEntity<CashResponse> deposit(
            @CurrentUser UUID accountId,
            @Valid @RequestBody CashRequest request) {
        return ResponseEntity.ok(cashService.deposit(accountId, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CashResponse> withdraw(
            @CurrentUser UUID accountId,
            @Valid @RequestBody CashRequest request) {
        return ResponseEntity.ok(cashService.withdraw(accountId, request));
    }
}
