package com.splitwise.expense.service;

import com.splitwise.expense.dto.*;
import com.splitwise.expense.exception.BadRequestException;
import com.splitwise.expense.exception.ResourceNotFoundException;
import com.splitwise.expense.exception.UnauthorizedException;
import com.splitwise.expense.model.Expense;
import com.splitwise.expense.model.ExpenseSplit;
import com.splitwise.expense.repository.ExpenseRepository;
import com.splitwise.expense.repository.ExpenseSplitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final SplitCalculatorService splitCalculatorService;

    /**
     * Create a new expense with calculated splits
     */
    public ExpenseResponse createExpense(CreateExpenseRequest request, String currentUserId) {
        log.info("Creating expense for group: {}, paid by: {}", request.getGroupId(), request.getPaidBy());

        // Validate that the current user is the payer
        if (!request.getPaidBy().equals(currentUserId)) {
            throw new UnauthorizedException("You can only create expenses that you paid for");
        }

        // Calculate splits using the split calculator
        List<ExpenseSplit> splits = splitCalculatorService.calculateSplits(request);

        // Create expense entity
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .groupId(request.getGroupId())
                .paidBy(request.getPaidBy())
                .category(request.getCategory())
                .splitType(request.getSplitType())
                .receiptUrl(request.getReceiptUrl())
                .notes(request.getNotes())
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .splits(splits)
                .build();

        // Set the expense reference in each split (bidirectional relationship)
        splits.forEach(split -> split.setExpense(expense));

        // Save expense (cascades to splits)
        Expense savedExpense = expenseRepository.save(expense);

        log.info("Expense created successfully with ID: {}", savedExpense.getId());
        return convertToResponse(savedExpense);
    }

    /**
     * Get expense by ID
     */
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId, String currentUserId) {
        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        // Check if user has access to this expense (either payer or participant)
        boolean hasAccess = expense.getPaidBy().equals(currentUserId) ||
                expense.getSplits().stream().anyMatch(split -> split.getUserId().equals(currentUserId));

        if (!hasAccess) {
            throw new UnauthorizedException("You don't have access to this expense");
        }

        return convertToResponse(expense);
    }

    /**
     * Get all expenses for a group
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        List<Expense> expenses = expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(groupId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all expenses for a user (across all groups)
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses(String userId) {
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(userId);
        return expenses.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing expense
     */
    public ExpenseResponse updateExpense(Long expenseId, CreateExpenseRequest request, String currentUserId) {
        log.info("Updating expense ID: {}", expenseId);

        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        // Only the payer can update the expense
        if (!expense.getPaidBy().equals(currentUserId)) {
            throw new UnauthorizedException("Only the payer can update this expense");
        }

        // Recalculate splits
        List<ExpenseSplit> newSplits = splitCalculatorService.calculateSplits(request);

        // Update expense fields
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        expense.setCategory(request.getCategory());
        expense.setSplitType(request.getSplitType());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setNotes(request.getNotes());
        expense.setUpdatedAt(LocalDateTime.now());

        // Clear old splits and add new ones
        expense.getSplits().clear();
        newSplits.forEach(split -> {
            split.setExpense(expense);
            expense.getSplits().add(split);
        });

        Expense updatedExpense = expenseRepository.save(expense);
        log.info("Expense updated successfully: {}", expenseId);

        return convertToResponse(updatedExpense);
    }

    /**
     * Delete an expense (soft delete)
     */
    public void deleteExpense(Long expenseId, String currentUserId) {
        log.info("Deleting expense ID: {}", expenseId);

        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        // Only the payer can delete the expense
        if (!expense.getPaidBy().equals(currentUserId)) {
            throw new UnauthorizedException("Only the payer can delete this expense");
        }

        expense.setIsActive(false);
        expense.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(expense);

        log.info("Expense soft-deleted successfully: {}", expenseId);
    }

    /**
     * Calculate balance for a user across all their expenses
     */
    @Transactional(readOnly = true)
    public UserBalanceResponse calculateUserBalance(String userId) {
        log.info("Calculating balance for user: {}", userId);

        // Get total amount user owes (from their splits)
        BigDecimal totalOwed = expenseSplitRepository.getTotalOwedByUser(userId);

        // Get total amount user paid (expenses they created)
        BigDecimal totalPaid = expenseSplitRepository.getTotalPaidByUser(userId);

        // FIX: Handle null values for new users (database SUM returns null when no rows
        // match)
        BigDecimal totalOwedValue = totalOwed != null ? totalOwed : BigDecimal.ZERO;
        BigDecimal totalPaidValue = totalPaid != null ? totalPaid : BigDecimal.ZERO;

        // Calculate net balance (positive = others owe you, negative = you owe others)
        BigDecimal netBalance = totalPaidValue.subtract(totalOwedValue);

        log.debug("Balance calculated for user {} - Paid: {}, Owed: {}, Net: {}",
                userId, totalPaidValue, totalOwedValue, netBalance);

        // Get detailed balances per user
        Map<String, BigDecimal> balances = calculateDetailedBalances(userId);

        return UserBalanceResponse.builder()
                .userId(userId)
                .totalPaid(totalPaidValue)
                .totalOwed(totalOwedValue)
                .netBalance(netBalance)
                .balances(balances)
                .build();
    }

    /**
     * Calculate detailed balances between users
     */
    private Map<String, BigDecimal> calculateDetailedBalances(String userId) {
        Map<String, BigDecimal> balances = new HashMap<>();

        // Get all expenses where this user is involved
        List<Expense> userExpenses = expenseRepository.findByUserIdOrderByDateDesc(userId);

        for (Expense expense : userExpenses) {
            String paidBy = expense.getPaidBy();

            for (ExpenseSplit split : expense.getSplits()) {
                String splitUserId = split.getUserId();

                // Skip if it's the same user
                if (splitUserId.equals(userId)) {
                    continue;
                }

                BigDecimal amount = split.getAmount();

                if (paidBy.equals(userId)) {
                    // This user paid, so others owe them
                    balances.merge(splitUserId, amount, BigDecimal::add);
                } else {
                    // Someone else paid, this user owes them
                    balances.merge(paidBy, amount.negate(), BigDecimal::add);
                }
            }
        }

        return balances;
    }

    /**
     * Calculate net balances for all users in a group
     * Returns Map<userId, netBalance>
     * Positive balance = others owe this user
     * Negative balance = this user owes others
     */
    @Transactional(readOnly = true)
    public Map<String, BigDecimal> calculateGroupBalances(Long groupId) {
        log.info("Calculating balances for group: {}", groupId);

        Map<String, BigDecimal> balances = new HashMap<>();

        // Get all expenses for the group
        List<Expense> expenses = expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(groupId);

        for (Expense expense : expenses) {
            String paidBy = expense.getPaidBy();

            // Add the total amount paid to the payer's balance
            balances.merge(paidBy, expense.getAmount(), BigDecimal::add);

            // Subtract each split from the respective user's balance
            for (ExpenseSplit split : expense.getSplits()) {
                balances.merge(split.getUserId(), split.getAmount().negate(), BigDecimal::add);
            }
        }

        // Filter out zero balances
        balances.entrySet().removeIf(entry -> entry.getValue().compareTo(BigDecimal.ZERO) == 0);

        log.info("Calculated balances for {} users in group {}", balances.size(), groupId);
        return balances;
    }

    /**
     * Convert Expense entity to ExpenseResponse DTO
     */
    private ExpenseResponse convertToResponse(Expense expense) {
        List<ExpenseResponse.SplitResponse> splitResponses = expense.getSplits().stream()
                .map(split -> ExpenseResponse.SplitResponse.builder()
                        .userId(split.getUserId())
                        .amount(split.getAmount())
                        .percentage(split.getPercentage())
                        .isPaid(split.isPaid())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .groupId(expense.getGroupId())
                .paidBy(expense.getPaidBy())
                .category(expense.getCategory())
                .splitType(expense.getSplitType())
                .receiptUrl(expense.getReceiptUrl())
                .notes(expense.getNotes())
                .date(expense.getDate())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .splits(splitResponses)
                .build();
    }
}
