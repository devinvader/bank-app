package ru.devinvader.bank.gateway.config;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallback/accounts")
    public ResponseEntity<Map<String, String>> accountsFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Accounts service unavailable"));
    }

    @GetMapping("/fallback/cash")
    public ResponseEntity<Map<String, String>> cashFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Cash service unavailable"));
    }

    @GetMapping("/fallback/transfer")
    public ResponseEntity<Map<String, String>> transferFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Transfer service unavailable"));
    }

    @GetMapping("/fallback/notifications")
    public ResponseEntity<Map<String, String>> notificationsFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Notifications service unavailable"));
    }
}
