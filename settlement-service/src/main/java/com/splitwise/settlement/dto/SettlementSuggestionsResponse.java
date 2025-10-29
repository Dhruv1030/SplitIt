package com.splitwise.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response containing simplified settlement suggestions for a group
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementSuggestionsResponse {

    private Long groupId;
    private List<SettlementSuggestion> suggestions;
    private Integer totalTransactions;
    private BigDecimal totalAmount;
    private String currency;
    private String message;

    public SettlementSuggestionsResponse(Long groupId, List<SettlementSuggestion> suggestions) {
        this.groupId = groupId;
        this.suggestions = suggestions;
        this.totalTransactions = suggestions.size();
        this.totalAmount = suggestions.stream()
                .map(SettlementSuggestion::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.currency = suggestions.isEmpty() ? "USD" : suggestions.get(0).getCurrency();
        this.message = totalTransactions == 0
                ? "All settled up! 🎉"
                : String.format("%d transaction(s) needed to settle all debts", totalTransactions);
    }
}
