package com.splitwise.expense.dto;

import com.splitwise.expense.model.SplitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String currency;
    private Long groupId;
    private String paidBy;
    private String createdBy;
    private String category;
    private SplitType splitType;
    private String receiptUrl;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private String notes;
    private List<SplitResponse> splits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SplitResponse {
        private Long id;
        private String userId;
        private BigDecimal amount;
        private BigDecimal percentage;
        private boolean isPaid;
    }
}
