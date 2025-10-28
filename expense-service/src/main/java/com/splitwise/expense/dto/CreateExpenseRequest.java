package com.splitwise.expense.dto;

import com.splitwise.expense.model.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private String currency;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotBlank(message = "Paid by user ID is required")
    private String paidBy;

    private String category;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    private String receiptUrl;

    private String notes;

    // For EQUAL split: list of participant user IDs
    private List<String> participantIds;

    // For EXACT split: map of userId -> exact amount
    private Map<String, BigDecimal> exactAmounts;

    // For PERCENTAGE split: map of userId -> percentage
    private Map<String, BigDecimal> percentages;
}
