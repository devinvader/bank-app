package ru.devinvader.bank.transfer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.devinvader.bank.common.security.CurrentUser;
import ru.devinvader.bank.transfer.model.TransferRequest;
import ru.devinvader.bank.transfer.model.TransferResponse;
import ru.devinvader.bank.transfer.service.TransferService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> execute(
            @CurrentUser UUID accountId,
            @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.execute(accountId, request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransferResponse>> getHistory(@CurrentUser UUID accountId) {
        return ResponseEntity.ok(transferService.getHistory(accountId));
    }
}
