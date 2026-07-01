package ru.devinvader.bank.gateway.config;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/accounts")
    public ResponseEntity<Map<String, String>> accountsFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Accounts service unavailable"));
    }

    @RequestMapping("/fallback/cash")
    public ResponseEntity<Map<String, String>> cashFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Cash service unavailable"));
    }

    @RequestMapping("/fallback/transfer")
    public ResponseEntity<Map<String, String>> transferFallback() {
        return ResponseEntity.status(503).body(Map.of("error", "Transfer service unavailable"));
    }

}
