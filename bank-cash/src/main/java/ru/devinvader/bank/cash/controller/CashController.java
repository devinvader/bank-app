package ru.devinvader.bank.cash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.cash.model.CashRequest;
import ru.devinvader.bank.cash.model.CashResponse;
import ru.devinvader.bank.cash.service.CashService;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/deposit")
    public ResponseEntity<CashResponse> deposit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CashRequest request) {
        var login = jwt.getClaimAsString("preferred_username");
        var response = cashService.deposit(login, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<CashResponse> withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CashRequest request) {
        var login = jwt.getClaimAsString("preferred_username");
        var response = cashService.withdraw(login, request);
        return ResponseEntity.ok(response);
    }
}
