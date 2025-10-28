package com.splitwise.expense.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String paidBy; // User ID who paid

    @Column(length = 50)
    private String category; // FOOD, TRANSPORT, ACCOMMODATION, ENTERTAINMENT, OTHER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SplitType splitType;

    @Column(length = 500)
    private String receiptUrl;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime date = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseSplit> splits = new ArrayList<>();

    private String notes;
}
