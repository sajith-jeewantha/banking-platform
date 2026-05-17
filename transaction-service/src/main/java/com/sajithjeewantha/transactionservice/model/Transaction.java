package com.sajithjeewantha.transactionservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique transaction reference (UUID)
    @Column(unique = true, nullable = false)
    private String transactionRef;

    // The user who initiated the transaction (from JWT sub)
    @Column(nullable = false)
    private String initiatedBy;

    private Long fromAccountId;  // null for DEPOSIT
    private Long toAccountId;    // null for WITHDRAWAL

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private String description;

    // Error message if failed
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        TRANSFER,    // from one account to another
        DEPOSIT,     // money coming in
        WITHDRAWAL   // money going out
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
