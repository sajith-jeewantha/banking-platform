package com.sajithjeewantha.accountservice.service;

import com.sajithjeewantha.accountservice.dto.AccountDTO;
import com.sajithjeewantha.accountservice.model.Account;
import com.sajithjeewantha.accountservice.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    // ─── Create Account ────────────────────────────────────────
    @Transactional
    public AccountDTO.AccountResponse createAccount(
            AccountDTO.CreateAccountRequest request, String userId) {

        log.info("Creating account for user: {}", userId);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(userId)
                .ownerName(request.getOwnerName())
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit())
                .status(Account.AccountStatus.ACTIVE)
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());
        return AccountDTO.AccountResponse.from(saved);
    }

    // ─── Get Account by ID ────────────────────────────────────
    public AccountDTO.AccountResponse getAccountById(Long id, String userId) {
        Account account = findAccountById(id);
        // Users can only view their own accounts
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: account does not belong to this user");
        }
        return AccountDTO.AccountResponse.from(account);
    }

    // ─── Get All Accounts for User ────────────────────────────
    public List<AccountDTO.AccountResponse> getAccountsByUser(String userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountDTO.AccountResponse::from)
                .collect(Collectors.toList());
    }

    // ─── Get Balance ──────────────────────────────────────────
    public BigDecimal getBalance(Long id, String userId) {
        Account account = findAccountById(id);
        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        return account.getBalance();
    }

    // ─── Internal: Update Balance (called by Transaction Service) ─
    @Transactional
    public AccountDTO.AccountResponse updateBalance(Long id,
            AccountDTO.BalanceUpdateRequest request) {

        Account account = findAccountById(id);

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active: " + account.getAccountNumber());
        }

        if (request.getOperation() == AccountDTO.BalanceUpdateRequest.BalanceOperation.DEBIT) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient balance in account: "
                        + account.getAccountNumber());
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
            log.info("Debited {} from account {}", request.getAmount(), account.getAccountNumber());
        } else {
            account.setBalance(account.getBalance().add(request.getAmount()));
            log.info("Credited {} to account {}", request.getAmount(), account.getAccountNumber());
        }

        return AccountDTO.AccountResponse.from(accountRepository.save(account));
    }

    // ─── Internal: Get Account by ID (for Transaction Service) ──
    public AccountDTO.AccountResponse getAccountByIdInternal(Long id) {
        return AccountDTO.AccountResponse.from(findAccountById(id));
    }

    // ─── Helper ───────────────────────────────────────────────
    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }

    private String generateAccountNumber() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(999999));
        String accountNumber = "ACC" + timestamp + random;

        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(accountNumber)) {
            random = String.format("%06d", new Random().nextInt(999999));
            accountNumber = "ACC" + timestamp + random;
        }
        return accountNumber;
    }
}
