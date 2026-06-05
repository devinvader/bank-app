package ru.devinvader.bank.accounts.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.accounts.model.AccountRequest;
import ru.devinvader.bank.accounts.model.AccountResponse;
import ru.devinvader.bank.accounts.model.BalanceRequest;
import ru.devinvader.bank.accounts.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountResponse> getCurrentAccount(@AuthenticationPrincipal Jwt jwt) {
        var login = jwt.getClaimAsString("preferred_username");
        var response = accountService.getByLogin(login);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{login}")
    public ResponseEntity<AccountResponse> getByLogin(@PathVariable String login) {
        var response = accountService.getByLogin(login);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<AccountResponse> updateCurrentAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AccountRequest request) {
        var login = jwt.getClaimAsString("preferred_username");
        var response = accountService.update(login, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transfer-targets")
    public ResponseEntity<List<AccountResponse>> getTransferTargets(@AuthenticationPrincipal Jwt jwt) {
        var login = jwt.getClaimAsString("preferred_username");
        var targets = accountService.getTransferTargets(login);
        return ResponseEntity.ok(targets);
    }

    @PostMapping("/{login}/debit")
    public ResponseEntity<?> debit(
            @PathVariable String login,
            @Valid @RequestBody BalanceRequest request) {
        accountService.debit(login, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{login}/credit")
    public ResponseEntity<?> credit(
            @PathVariable String login,
            @Valid @RequestBody BalanceRequest request) {
        accountService.credit(login, request.amount());
        return ResponseEntity.ok().build();
    }
}
