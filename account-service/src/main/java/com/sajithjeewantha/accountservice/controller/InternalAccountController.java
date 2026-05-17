package com.sajithjeewantha.accountservice.controller;

import com.sajithjeewantha.accountservice.dto.AccountDTO;
import com.sajithjeewantha.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoints called ONLY by Transaction Service (not exposed via APIM).
 * Protected by a shared secret header instead of JWT.
 */
@RestController
@RequestMapping("/internal/accounts")
@RequiredArgsConstructor
@Slf4j
public class InternalAccountController {

    private final AccountService accountService;

    @Value("${internal.service.secret}")
    private String internalSecret;

    // ─── GET /internal/accounts/{id} ─────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO.AccountResponse> getAccount(
            @PathVariable Long id,
            @RequestHeader("X-Internal-Secret") String secret) {

        validateSecret(secret);
        return ResponseEntity.ok(accountService.getAccountByIdInternal(id));
    }

    // ─── PUT /internal/accounts/{id}/balance ──────────────────
    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountDTO.AccountResponse> updateBalance(
            @PathVariable Long id,
            @Valid @RequestBody AccountDTO.BalanceUpdateRequest request,
            @RequestHeader("X-Internal-Secret") String secret) {

        validateSecret(secret);
        log.info("Internal balance update for account {}: {} {}", id,
                request.getOperation(), request.getAmount());
        return ResponseEntity.ok(accountService.updateBalance(id, request));
    }

    private void validateSecret(String secret) {
        if (!internalSecret.equals(secret)) {
            throw new RuntimeException("Unauthorized internal request");
        }
    }
}
