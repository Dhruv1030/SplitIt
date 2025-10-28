package com.splitwise.expense.service;

import com.splitwise.expense.dto.CreateExpenseRequest;
import com.splitwise.expense.model.ExpenseSplit;
import com.splitwise.expense.model.SplitType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class SplitCalculatorService {

    /**
     * Calculate splits based on the split type
     */
    public List<ExpenseSplit> calculateSplits(CreateExpenseRequest request) {
        log.info("Calculating splits for expense of amount: {} with type: {}",
                request.getAmount(), request.getSplitType());

        return switch (request.getSplitType()) {
            case EQUAL -> calculateEqualSplits(request);
            case EXACT -> calculateExactSplits(request);
            case PERCENTAGE -> calculatePercentageSplits(request);
        };
    }

    /**
     * EQUAL SPLIT: Divide amount equally among all participants
     * Example: $120 ÷ 3 = $40 each
     */
    private List<ExpenseSplit> calculateEqualSplits(CreateExpenseRequest request) {
        if (request.getParticipantIds() == null || request.getParticipantIds().isEmpty()) {
            throw new IllegalArgumentException("Participant IDs are required for equal split");
        }

        List<ExpenseSplit> splits = new ArrayList<>();
        BigDecimal totalAmount = request.getAmount();
        int participantCount = request.getParticipantIds().size();

        // Calculate per-person amount
        BigDecimal perPersonAmount = totalAmount.divide(
                BigDecimal.valueOf(participantCount),
                2,
                RoundingMode.HALF_UP);

        log.debug("Equal split: {} participants, {} per person", participantCount, perPersonAmount);

        // Handle rounding differences
        BigDecimal allocatedAmount = BigDecimal.ZERO;

        for (int i = 0; i < request.getParticipantIds().size(); i++) {
            String userId = request.getParticipantIds().get(i);
            BigDecimal amount;

            if (i == request.getParticipantIds().size() - 1) {
                // Last person gets the remainder to handle rounding
                amount = totalAmount.subtract(allocatedAmount);
            } else {
                amount = perPersonAmount;
                allocatedAmount = allocatedAmount.add(amount);
            }

            ExpenseSplit split = ExpenseSplit.builder()
                    .userId(userId)
                    .amount(amount)
                    .percentage(null)
                    .isPaid(userId.equals(request.getPaidBy())) // Payer is already paid
                    .build();

            splits.add(split);
        }

        return splits;
    }

    /**
     * EXACT SPLIT: Each person has a specific amount
     * Example: Alice: $40, Bob: $50, Charlie: $30 = $120
     */
    private List<ExpenseSplit> calculateExactSplits(CreateExpenseRequest request) {
        if (request.getExactAmounts() == null || request.getExactAmounts().isEmpty()) {
            throw new IllegalArgumentException("Exact amounts are required for exact split");
        }

        // Validate that exact amounts sum to total
        BigDecimal sum = request.getExactAmounts().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException(
                    String.format("Sum of exact amounts (%s) must equal total amount (%s)",
                            sum, request.getAmount()));
        }

        List<ExpenseSplit> splits = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : request.getExactAmounts().entrySet()) {
            ExpenseSplit split = ExpenseSplit.builder()
                    .userId(entry.getKey())
                    .amount(entry.getValue())
                    .percentage(null)
                    .isPaid(entry.getKey().equals(request.getPaidBy()))
                    .build();

            splits.add(split);
        }

        log.debug("Exact split created for {} participants", splits.size());
        return splits;
    }

    /**
     * PERCENTAGE SPLIT: Each person pays a percentage of total
     * Example: Alice: 50%, Bob: 30%, Charlie: 20% of $120
     */
    private List<ExpenseSplit> calculatePercentageSplits(CreateExpenseRequest request) {
        if (request.getPercentages() == null || request.getPercentages().isEmpty()) {
            throw new IllegalArgumentException("Percentages are required for percentage split");
        }

        // Validate that percentages sum to 100
        BigDecimal sumPercentage = request.getPercentages().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sumPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException(
                    String.format("Sum of percentages (%s) must equal 100", sumPercentage));
        }

        List<ExpenseSplit> splits = new ArrayList<>();
        BigDecimal totalAmount = request.getAmount();
        BigDecimal allocatedAmount = BigDecimal.ZERO;

        List<Map.Entry<String, BigDecimal>> entries = new ArrayList<>(request.getPercentages().entrySet());

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, BigDecimal> entry = entries.get(i);
            BigDecimal amount;

            if (i == entries.size() - 1) {
                // Last person gets the remainder to handle rounding
                amount = totalAmount.subtract(allocatedAmount);
            } else {
                // Calculate amount based on percentage
                amount = totalAmount
                        .multiply(entry.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                allocatedAmount = allocatedAmount.add(amount);
            }

            ExpenseSplit split = ExpenseSplit.builder()
                    .userId(entry.getKey())
                    .amount(amount)
                    .percentage(entry.getValue())
                    .isPaid(entry.getKey().equals(request.getPaidBy()))
                    .build();

            splits.add(split);
        }

        log.debug("Percentage split created for {} participants", splits.size());
        return splits;
    }

    /**
     * Validate that all split amounts are positive
     */
    public void validateSplits(List<ExpenseSplit> splits) {
        for (ExpenseSplit split : splits) {
            if (split.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Split amount must be greater than zero for user: " + split.getUserId());
            }
        }
    }
}
