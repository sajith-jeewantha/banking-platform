package com.sajithjeewantha.transactionservice.controller;

import com.sajithjeewantha.transactionservice.dto.TransactionDTO;
import com.sajithjeewantha.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    // ─── POST /api/transactions/transfer ──────────────────────
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO.ApiResponse<TransactionDTO.TransactionResponse>> transfer(
            @Valid @RequestBody TransactionDTO.TransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        TransactionDTO.TransactionResponse response = transactionService.transfer(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionDTO.ApiResponse.success("Transfer processed", response));
    }

    // ─── POST /api/transactions/deposit ───────────────────────
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDTO.ApiResponse<TransactionDTO.TransactionResponse>> deposit(
            @Valid @RequestBody TransactionDTO.DepositRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        TransactionDTO.TransactionResponse response = transactionService.deposit(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionDTO.ApiResponse.success("Deposit processed", response));
    }

    // ─── POST /api/transactions/withdraw ──────────────────────
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDTO.ApiResponse<TransactionDTO.TransactionResponse>> withdraw(
            @Valid @RequestBody TransactionDTO.WithdrawalRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        TransactionDTO.TransactionResponse response = transactionService.withdraw(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionDTO.ApiResponse.success("Withdrawal processed", response));
    }

    // ─── GET /api/transactions/my ──────────────────────────────
    // Get all transactions initiated by the logged-in user
    @GetMapping("/my")
    public ResponseEntity<TransactionDTO.ApiResponse<List<TransactionDTO.TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        List<TransactionDTO.TransactionResponse> transactions =
                transactionService.getMyTransactions(userId);
        return ResponseEntity.ok(
                TransactionDTO.ApiResponse.success("Transactions retrieved", transactions));
    }

    // ─── GET /api/transactions/account/{accountId} ────────────
    // Get all transactions for a specific account
    @GetMapping("/account/{accountId}")
    public ResponseEntity<TransactionDTO.ApiResponse<List<TransactionDTO.TransactionResponse>>> getByAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal Jwt jwt) {

        List<TransactionDTO.TransactionResponse> transactions =
                transactionService.getTransactionsByAccount(accountId);
        return ResponseEntity.ok(
                TransactionDTO.ApiResponse.success("Transactions retrieved", transactions));
    }

    // ─── GET /api/transactions/{ref} ──────────────────────────
    // Get a single transaction by reference
    @GetMapping("/{ref}")
    public ResponseEntity<TransactionDTO.ApiResponse<TransactionDTO.TransactionResponse>> getByRef(
            @PathVariable String ref,
            @AuthenticationPrincipal Jwt jwt) {

        TransactionDTO.TransactionResponse response = transactionService.getTransactionByRef(ref);
        return ResponseEntity.ok(
                TransactionDTO.ApiResponse.success("Transaction retrieved", response));
    }
}
