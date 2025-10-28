package com.splitwise.expense.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage; // For percentage-based splits

    @Builder.Default
    @Column(nullable = false)
    private boolean isPaid = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonIgnore
    private Expense expense;
}
