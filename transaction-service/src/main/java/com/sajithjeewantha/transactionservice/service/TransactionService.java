package com.sajithjeewantha.transactionservice.service;

import com.sajithjeewantha.transactionservice.client.AccountServiceClient;
import com.sajithjeewantha.transactionservice.dto.TransactionDTO;
import com.sajithjeewantha.transactionservice.model.Transaction;
import com.sajithjeewantha.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;

    // ─── TRANSFER ─────────────────────────────────────────────
    @Transactional
    public TransactionDTO.TransactionResponse transfer(
            TransactionDTO.TransferRequest request, String userId) {

        log.info("Transfer request: {} -> {}, amount: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount());

        // Create transaction record (PENDING first)
        Transaction transaction = Transaction.builder()
                .transactionRef(UUID.randomUUID().toString())
                .initiatedBy(userId)
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription())
                .build();

        transactionRepository.save(transaction);

        try {
            // Verify from account belongs to this user
            TransactionDTO.AccountInfo fromAccount =
                    accountServiceClient.getAccount(request.getFromAccountId());

            if (!fromAccount.getUserId().equals(userId)) {
                throw new RuntimeException("You can only transfer from your own account");
            }

            // Verify to account exists
            accountServiceClient.getAccount(request.getToAccountId());

            // Debit sender
            accountServiceClient.debitAccount(request.getFromAccountId(), request.getAmount());

            // Credit receiver
            accountServiceClient.creditAccount(request.getToAccountId(), request.getAmount());

            // Mark SUCCESS
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transaction.setCompletedAt(LocalDateTime.now());
            log.info("Transfer SUCCESS: {}", transaction.getTransactionRef());

        } catch (Exception e) {
            // Mark FAILED
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            log.error("Transfer FAILED: {} - {}", transaction.getTransactionRef(), e.getMessage());
        }

        return TransactionDTO.TransactionResponse.from(transactionRepository.save(transaction));
    }

    // ─── DEPOSIT ──────────────────────────────────────────────
    @Transactional
    public TransactionDTO.TransactionResponse deposit(
            TransactionDTO.DepositRequest request, String userId) {

        log.info("Deposit request: account {}, amount: {}",
                request.getToAccountId(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .transactionRef(UUID.randomUUID().toString())
                .initiatedBy(userId)
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription() != null ?
                        request.getDescription() : "Deposit")
                .build();

        transactionRepository.save(transaction);

        try {
            // Credit the account
            accountServiceClient.creditAccount(request.getToAccountId(), request.getAmount());

            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transaction.setCompletedAt(LocalDateTime.now());
            log.info("Deposit SUCCESS: {}", transaction.getTransactionRef());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            log.error("Deposit FAILED: {}", e.getMessage());
        }

        return TransactionDTO.TransactionResponse.from(transactionRepository.save(transaction));
    }

    // ─── WITHDRAWAL ───────────────────────────────────────────
    @Transactional
    public TransactionDTO.TransactionResponse withdraw(
            TransactionDTO.WithdrawalRequest request, String userId) {

        log.info("Withdrawal request: account {}, amount: {}",
                request.getFromAccountId(), request.getAmount());

        Transaction transaction = Transaction.builder()
                .transactionRef(UUID.randomUUID().toString())
                .initiatedBy(userId)
                .fromAccountId(request.getFromAccountId())
                .amount(request.getAmount())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .status(Transaction.TransactionStatus.PENDING)
                .description(request.getDescription() != null ?
                        request.getDescription() : "Withdrawal")
                .build();

        transactionRepository.save(transaction);

        try {
            // Verify account belongs to user
            TransactionDTO.AccountInfo account =
                    accountServiceClient.getAccount(request.getFromAccountId());

            if (!account.getUserId().equals(userId)) {
                throw new RuntimeException("You can only withdraw from your own account");
            }

            // Debit the account
            accountServiceClient.debitAccount(request.getFromAccountId(), request.getAmount());

            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            transaction.setCompletedAt(LocalDateTime.now());
            log.info("Withdrawal SUCCESS: {}", transaction.getTransactionRef());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction.setCompletedAt(LocalDateTime.now());
            log.error("Withdrawal FAILED: {}", e.getMessage());
        }

        return TransactionDTO.TransactionResponse.from(transactionRepository.save(transaction));
    }

    // ─── GET TRANSACTION HISTORY ──────────────────────────────
    public List<TransactionDTO.TransactionResponse> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionDTO.TransactionResponse::from)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO.TransactionResponse> getMyTransactions(String userId) {
        return transactionRepository.findByInitiatedByOrderByCreatedAtDesc(userId)
                .stream()
                .map(TransactionDTO.TransactionResponse::from)
                .collect(Collectors.toList());
    }

    public TransactionDTO.TransactionResponse getTransactionByRef(String ref) {
        return transactionRepository.findAll().stream()
                .filter(t -> t.getTransactionRef().equals(ref))
                .findFirst()
                .map(TransactionDTO.TransactionResponse::from)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + ref));
    }
}
