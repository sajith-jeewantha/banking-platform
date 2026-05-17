package com.sajithjeewantha.transactionservice.dto;

import com.sajithjeewantha.transactionservice.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    // ─── Transfer Request ──────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class TransferRequest {

        @NotNull(message = "From account ID is required")
        private Long fromAccountId;

        @NotNull(message = "To account ID is required")
        private Long toAccountId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    // ─── Deposit Request ──────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DepositRequest {

        @NotNull(message = "Account ID is required")
        private Long toAccountId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    // ─── Withdrawal Request ───────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class WithdrawalRequest {

        @NotNull(message = "Account ID is required")
        private Long fromAccountId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    // ─── Transaction Response ─────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TransactionResponse {
        private Long id;
        private String transactionRef;
        private String initiatedBy;
        private Long fromAccountId;
        private Long toAccountId;
        private BigDecimal amount;
        private Transaction.TransactionType type;
        private Transaction.TransactionStatus status;
        private String description;
        private String failureReason;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public static TransactionResponse from(Transaction t) {
            return TransactionResponse.builder()
                    .id(t.getId())
                    .transactionRef(t.getTransactionRef())
                    .initiatedBy(t.getInitiatedBy())
                    .fromAccountId(t.getFromAccountId())
                    .toAccountId(t.getToAccountId())
                    .amount(t.getAmount())
                    .type(t.getType())
                    .status(t.getStatus())
                    .description(t.getDescription())
                    .failureReason(t.getFailureReason())
                    .createdAt(t.getCreatedAt())
                    .completedAt(t.getCompletedAt())
                    .build();
        }
    }

    // ─── API Response Wrapper ──────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).build();
        }
    }

    // ─── Account DTO (received from Account Service) ──────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class AccountInfo {
        private Long id;
        private String accountNumber;
        private String userId;
        private String ownerName;
        private String accountType;
        private java.math.BigDecimal balance;
        private String status;
    }
}
