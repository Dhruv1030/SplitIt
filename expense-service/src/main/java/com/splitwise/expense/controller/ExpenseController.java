package com.splitwise.expense.controller;

import com.splitwise.expense.dto.ApiResponse;
import com.splitwise.expense.dto.CreateExpenseRequest;
import com.splitwise.expense.dto.ExpenseResponse;
import com.splitwise.expense.dto.UserBalanceResponse;
import com.splitwise.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Create a new expense
     * Header: X-User-Id (from JWT via API Gateway)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Creating expense for user: {}", userId);
        ExpenseResponse response = expenseService.createExpense(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense created successfully", response));
    }

    /**
     * Get expense by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpense(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching expense ID: {} for user: {}", id, userId);
        ExpenseResponse response = expenseService.getExpenseById(id, userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all expenses for a group
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getGroupExpenses(
            @PathVariable Long groupId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching expenses for group: {}", groupId);
        List<ExpenseResponse> expenses = expenseService.getGroupExpenses(groupId);

        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    /**
     * Get all expenses for current user
     */
    @GetMapping("/my-expenses")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getMyExpenses(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching expenses for user: {}", userId);
        List<ExpenseResponse> expenses = expenseService.getUserExpenses(userId);

        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    /**
     * Update an expense
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody CreateExpenseRequest request,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Updating expense ID: {} by user: {}", id, userId);
        ExpenseResponse response = expenseService.updateExpense(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    /**
     * Delete an expense (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Deleting expense ID: {} by user: {}", id, userId);
        expenseService.deleteExpense(id, userId);

        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    /**
     * Get user balance summary
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<UserBalanceResponse>> getUserBalance(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Calculating balance for user: {}", userId);
        UserBalanceResponse balance = expenseService.calculateUserBalance(userId);

        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /**
     * Get balance for specific user (admin/group feature)
     */
    @GetMapping("/balance/{userId}")
    public ResponseEntity<ApiResponse<UserBalanceResponse>> getUserBalanceById(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String currentUserId) {

        log.info("Calculating balance for user: {} requested by: {}", userId, currentUserId);
        UserBalanceResponse balance = expenseService.calculateUserBalance(userId);

        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    /**
     * Get balances for all users in a group (for settlement service)
     * Returns: Map<userId, netBalance>
     */
    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<Map<String, BigDecimal>> getGroupBalances(
            @PathVariable Long groupId) {

        log.info("Calculating balances for group: {}", groupId);
        Map<String, BigDecimal> balances = expenseService.calculateGroupBalances(groupId);

        return ResponseEntity.ok(balances);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Expense Service is running"));
    }
}
