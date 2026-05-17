package com.sajithjeewantha.accountservice.controller;

import com.sajithjeewantha.accountservice.dto.AccountDTO;
import com.sajithjeewantha.accountservice.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // ─── POST /api/accounts ────────────────────────────────────
    // Create a new bank account
    @PostMapping
    public ResponseEntity<AccountDTO.ApiResponse<AccountDTO.AccountResponse>> createAccount(
            @Valid @RequestBody AccountDTO.CreateAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject(); // 'sub' claim from WSO2 IS token
        log.info("Create account request from user: {}", userId);

        AccountDTO.AccountResponse response = accountService.createAccount(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccountDTO.ApiResponse.success("Account created successfully", response));
    }

    // ─── GET /api/accounts ─────────────────────────────────────
    // Get all accounts belonging to the logged-in user
    @GetMapping
    public ResponseEntity<AccountDTO.ApiResponse<List<AccountDTO.AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        List<AccountDTO.AccountResponse> accounts = accountService.getAccountsByUser(userId);
        return ResponseEntity.ok(AccountDTO.ApiResponse.success("Accounts retrieved", accounts));
    }

    // ─── GET /api/accounts/{id} ────────────────────────────────
    // Get a specific account by ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO.ApiResponse<AccountDTO.AccountResponse>> getAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        AccountDTO.AccountResponse response = accountService.getAccountById(id, userId);
        return ResponseEntity.ok(AccountDTO.ApiResponse.success("Account retrieved", response));
    }

    // ─── GET /api/accounts/{id}/balance ────────────────────────
    // Get balance of a specific account
    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountDTO.ApiResponse<BigDecimal>> getBalance(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        BigDecimal balance = accountService.getBalance(id, userId);
        return ResponseEntity.ok(AccountDTO.ApiResponse.success("Balance retrieved", balance));
    }
}
