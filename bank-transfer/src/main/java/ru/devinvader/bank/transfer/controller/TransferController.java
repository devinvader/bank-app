package ru.devinvader.bank.transfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.service.TransferService;

import java.util.List;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> execute(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody TransferRequest request) {
        var login = jwt.getClaimAsString("preferred_username");
        var response = transferService.execute(login, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransferResponse>> getHistory(@AuthenticationPrincipal Jwt jwt) {
        var login = jwt.getClaimAsString("preferred_username");
        var history = transferService.getHistory(login);
        return ResponseEntity.ok(history);
    }
}
