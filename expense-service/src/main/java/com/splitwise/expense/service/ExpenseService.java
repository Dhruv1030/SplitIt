package com.splitwise.expense.service;

import com.splitwise.expense.client.ActivityClient;
import com.splitwise.expense.client.ActivityRequest;
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
    private final ActivityClient activityClient;

    /**
     * Create a new expense with calculated splits
     */
    public ExpenseResponse createExpense(CreateExpenseRequest request, String currentUserId) {
        log.info("Creating expense for group: {}, paid by: {}, recorded by: {}", request.getGroupId(),
                request.getPaidBy(), currentUserId);

        // Calculate splits using the split calculator
        List<ExpenseSplit> splits = splitCalculatorService.calculateSplits(request);

        // Create expense entity
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .groupId(request.getGroupId())
                .paidBy(request.getPaidBy())
                .createdBy(currentUserId)
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

        // Log activity
        logExpenseActivity("EXPENSE_ADDED", savedExpense);

        return convertToResponse(savedExpense);
    }

    /**
     * Get expense by ID
     */
    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long expenseId, String currentUserId) {
        Expense expense = expenseRepository.findByIdAndIsActiveTrue(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + expenseId));

        // Check if user has access to this expense (payer, creator, or participant)
        boolean hasAccess = expense.getPaidBy().equals(currentUserId) ||
                (expense.getCreatedBy() != null && expense.getCreatedBy().equals(currentUserId)) ||
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

        // The person who recorded the expense (or the payer) can update it
        boolean isCreator = currentUserId.equals(expense.getCreatedBy());
        boolean isPayer = currentUserId.equals(expense.getPaidBy());
        if (!isCreator && !isPayer) {
            throw new UnauthorizedException("Only the person who recorded or paid this expense can update it");
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

        // The person who recorded the expense (or the payer) can delete it
        boolean isCreator = currentUserId.equals(expense.getCreatedBy());
        boolean isPayer = currentUserId.equals(expense.getPaidBy());
        if (!isCreator && !isPayer) {
            throw new UnauthorizedException("Only the person who recorded or paid this expense can delete it");
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

        // What the user still owes others (their unpaid splits on expenses others paid)
        BigDecimal totalOwed = expenseSplitRepository.getTotalOwedByUser(userId);

        // What others still owe the user (unpaid splits of other people on expenses
        // this user paid)
        BigDecimal totalOwedToUser = expenseSplitRepository.getTotalOwedToUser(userId);

        // Handle null values for users with no matching rows
        BigDecimal totalOwedValue = totalOwed != null ? totalOwed : BigDecimal.ZERO;
        BigDecimal totalOwedToUserValue = totalOwedToUser != null ? totalOwedToUser : BigDecimal.ZERO;

        // Net balance: positive = others owe you more than you owe; negative = you owe
        // more
        BigDecimal netBalance = totalOwedToUserValue.subtract(totalOwedValue);

        log.debug("Balance calculated for user {} - OwedToUser: {}, Owed: {}, Net: {}",
                userId, totalOwedToUserValue, totalOwedValue, netBalance);

        // Get detailed balances per user
        Map<String, BigDecimal> balances = calculateDetailedBalances(userId);

        return UserBalanceResponse.builder()
                .userId(userId)
                .totalPaid(totalOwedToUserValue) // "owed to you" — what others owe you
                .totalOwed(totalOwedValue) // "you owe" — what you owe others
                .netBalance(netBalance)
                .balances(balances)
                .build();
    }

    /**
     * Calculate detailed balances between users.
     * Positive value for a key means that person owes the current user.
     * Negative value for a key means the current user owes that person.
     */
    private Map<String, BigDecimal> calculateDetailedBalances(String userId) {
        Map<String, BigDecimal> balances = new HashMap<>();

        // Get all expenses where this user is involved
        List<Expense> userExpenses = expenseRepository.findByUserIdOrderByDateDesc(userId);

        for (Expense expense : userExpenses) {
            String paidBy = expense.getPaidBy();

            if (paidBy.equals(userId)) {
                // Current user paid: every other participant owes them their split amount
                for (ExpenseSplit split : expense.getSplits()) {
                    if (!split.getUserId().equals(userId)) {
                        balances.merge(split.getUserId(), split.getAmount(), BigDecimal::add);
                    }
                }
            } else {
                // Someone else paid: find only THIS user's split — that's what they owe the
                // payer
                expense.getSplits().stream()
                        .filter(s -> s.getUserId().equals(userId))
                        .findFirst()
                        .ifPresent(mySplit -> balances.merge(paidBy, mySplit.getAmount().negate(), BigDecimal::add));
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
                .createdBy(expense.getCreatedBy())
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

    /**
     * Log activity to activity service
     */
    private void logExpenseActivity(String activityType, Expense expense) {
        try {
            String actor = expense.getCreatedBy() != null ? expense.getCreatedBy() : expense.getPaidBy();
            ActivityRequest activityRequest = ActivityRequest.builder()
                    .activityType(activityType)
                    .userId(actor)
                    .groupId(expense.getGroupId())
                    .description(String.format("%s added expense '%s' for %s %s (paid by %s)",
                            actor,
                            expense.getDescription(),
                            expense.getCurrency(),
                            expense.getAmount(),
                            expense.getPaidBy()))
                    .metadata(String.format("{\"expenseId\":%d,\"amount\":%s,\"category\":\"%s\"}",
                            expense.getId(),
                            expense.getAmount(),
                            expense.getCategory() != null ? expense.getCategory() : "OTHER"))
                    .build();

            activityClient.logActivity(activityRequest);
        } catch (Exception e) {
            log.warn("Failed to log activity: {}", e.getMessage());
        }
    }
}
