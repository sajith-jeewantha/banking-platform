package com.sajithjeewantha.accountservice.dto;


import com.sajithjeewantha.accountservice.model.Account;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AccountDTO {

    // ─── Request ───────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateAccountRequest {

        @NotBlank(message = "Owner name is required")
        private String ownerName;

        @NotNull(message = "Account type is required")
        private Account.AccountType accountType;

        @NotNull(message = "Initial deposit is required")
        @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
        private BigDecimal initialDeposit;
    }

    // ─── Response ──────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AccountResponse {
        private Long id;
        private String accountNumber;
        private String userId;
        private String ownerName;
        private Account.AccountType accountType;
        private BigDecimal balance;
        private Account.AccountStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static AccountResponse from(Account account) {
            return AccountResponse.builder()
                    .id(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .userId(account.getUserId())
                    .ownerName(account.getOwnerName())
                    .accountType(account.getAccountType())
                    .balance(account.getBalance())
                    .status(account.getStatus())
                    .createdAt(account.getCreatedAt())
                    .updatedAt(account.getUpdatedAt())
                    .build();
        }
    }

    // ─── Internal Balance Update (called by Transaction Service) ─
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class BalanceUpdateRequest {
        @NotNull
        private BigDecimal amount;

        @NotNull
        private BalanceOperation operation;

        public enum BalanceOperation {
            CREDIT, DEBIT
        }
    }

    // ─── API Response Wrapper ──────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
