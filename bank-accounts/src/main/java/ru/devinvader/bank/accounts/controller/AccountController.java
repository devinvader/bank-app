package ru.devinvader.bank.accounts.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;
import ru.devinvader.bank.accounts.model.BalanceRequest;
import ru.devinvader.bank.accounts.service.AccountService;
import ru.devinvader.bank.common.security.CurrentUser;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getCurrentAccount(@CurrentUser UUID accountId) {
        return ResponseEntity.ok(accountService.getById(accountId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getById(@PathVariable UUID accountId) {
        return ResponseEntity.ok(accountService.getById(accountId));
    }

    @PutMapping("/me")
    public ResponseEntity<AccountResponse> updateCurrentAccount(
            @CurrentUser UUID accountId,
            @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.ok(accountService.update(accountId, request));
    }

    @GetMapping("/transfer-targets")
    public ResponseEntity<List<AccountResponse>> getTransferTargets(@CurrentUser UUID accountId) {
        return ResponseEntity.ok(accountService.getTransferTargets(accountId));
    }

    @PostMapping("/{accountId}/debit")
    public ResponseEntity<?> debit(
            @PathVariable UUID accountId,
            @Valid @RequestBody BalanceRequest request) {
        accountService.debit(accountId, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/credit")
    public ResponseEntity<?> credit(
            @PathVariable UUID accountId,
            @Valid @RequestBody BalanceRequest request) {
        accountService.credit(accountId, request.amount());
        return ResponseEntity.ok().build();
    }
}
