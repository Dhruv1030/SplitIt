package com.splitwise.expense.service;

import com.splitwise.expense.client.ActivityClient;
import com.splitwise.expense.dto.*;
import com.splitwise.expense.exception.ResourceNotFoundException;
import com.splitwise.expense.exception.UnauthorizedException;
import com.splitwise.expense.model.Expense;
import com.splitwise.expense.model.ExpenseSplit;
import com.splitwise.expense.model.SplitType;
import com.splitwise.expense.repository.ExpenseRepository;
import com.splitwise.expense.repository.ExpenseSplitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseSplitRepository expenseSplitRepository;

    @Mock
    private SplitCalculatorService splitCalculatorService;

    @Mock
    private ActivityClient activityClient;

    @InjectMocks
    private ExpenseService expenseService;

    private CreateExpenseRequest createExpenseRequest;
    private Expense expense;
    private List<ExpenseSplit> expenseSplits;

    @BeforeEach
    void setUp() {
        createExpenseRequest = new CreateExpenseRequest();
        createExpenseRequest.setGroupId(1L);
        createExpenseRequest.setDescription("Dinner");
        createExpenseRequest.setAmount(new BigDecimal("100.00"));
        createExpenseRequest.setPaidBy("user1");
        createExpenseRequest.setSplitType(SplitType.EQUAL);
        createExpenseRequest.setParticipantIds(Arrays.asList("user1", "user2", "user3"));

        expense = Expense.builder()
                .id(1L)
                .groupId(1L)
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .paidBy("user1")
                .createdBy("user1")
                .splitType(SplitType.EQUAL)
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        expenseSplits = new ArrayList<>(Arrays.asList(
                ExpenseSplit.builder().id(1L).expense(expense).userId("user1")
                        .amount(new BigDecimal("33.33")).build(),
                ExpenseSplit.builder().id(2L).expense(expense).userId("user2")
                        .amount(new BigDecimal("33.33")).build(),
                ExpenseSplit.builder().id(3L).expense(expense).userId("user3")
                        .amount(new BigDecimal("33.34")).build()));

        expense.setSplits(new ArrayList<>(expenseSplits));
    }

    @Test
    void createExpense_WithEqualSplit_ShouldSucceed() {
        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        assertNotNull(response);
        assertEquals("Dinner", response.getDescription());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(SplitType.EQUAL, response.getSplitType());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_WithExactSplit_ShouldSucceed() {
        createExpenseRequest.setSplitType(SplitType.EXACT);
        Map<String, BigDecimal> exactAmounts = new HashMap<>();
        exactAmounts.put("user1", new BigDecimal("40.00"));
        exactAmounts.put("user2", new BigDecimal("30.00"));
        exactAmounts.put("user3", new BigDecimal("30.00"));
        createExpenseRequest.setExactAmounts(exactAmounts);

        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        assertNotNull(response);
        verify(splitCalculatorService).calculateSplits(any(CreateExpenseRequest.class));
    }

    @Test
    void createExpense_WithPercentageSplit_ShouldSucceed() {
        createExpenseRequest.setSplitType(SplitType.PERCENTAGE);
        Map<String, BigDecimal> percentages = new HashMap<>();
        percentages.put("user1", new BigDecimal("50"));
        percentages.put("user2", new BigDecimal("30"));
        percentages.put("user3", new BigDecimal("20"));
        createExpenseRequest.setPercentages(percentages);

        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        assertNotNull(response);
        verify(splitCalculatorService).calculateSplits(any(CreateExpenseRequest.class));
    }

    @Test
    void getExpenseById_WhenExists_ShouldReturnExpense() {
        when(expenseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(expense));

        ExpenseResponse response = expenseService.getExpenseById(1L, "user1");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Dinner", response.getDescription());
        assertEquals(3, response.getSplits().size());
    }

    @Test
    void getExpenseById_WhenNotExists_ShouldThrowException() {
        when(expenseRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpenseById(999L, "user1");
        });
    }

    @Test
    void getGroupExpenses_ShouldReturnAllExpenses() {
        when(expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(1L))
                .thenReturn(Arrays.asList(expense));

        List<ExpenseResponse> responses = expenseService.getGroupExpenses(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Dinner", responses.get(0).getDescription());
    }

    @Test
    void updateExpense_WhenAuthorized_ShouldSucceed() {
        when(expenseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(expense));
        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        CreateExpenseRequest updateRequest = new CreateExpenseRequest();
        updateRequest.setDescription("Updated Dinner");
        updateRequest.setAmount(new BigDecimal("120.00"));
        updateRequest.setPaidBy("user1");
        updateRequest.setSplitType(SplitType.EQUAL);
        updateRequest.setParticipantIds(Arrays.asList("user1", "user2"));

        ExpenseResponse response = expenseService.updateExpense(1L, updateRequest, "user1");

        assertNotNull(response);
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void updateExpense_WhenUnauthorized_ShouldThrowException() {
        when(expenseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(expense));

        CreateExpenseRequest updateRequest = new CreateExpenseRequest();
        updateRequest.setDescription("Updated Dinner");

        assertThrows(UnauthorizedException.class, () -> {
            expenseService.updateExpense(1L, updateRequest, "user2");
        });
    }

    @Test
    void deleteExpense_WhenAuthorized_ShouldSucceed() {
        when(expenseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        expenseService.deleteExpense(1L, "user1");

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void deleteExpense_WhenUnauthorized_ShouldThrowException() {
        when(expenseRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(expense));

        assertThrows(UnauthorizedException.class, () -> {
            expenseService.deleteExpense(1L, "user2");
        });
    }

    @Test
    void calculateUserBalance_ShouldReturnCorrectBalance() {
        // Service now uses repository queries for totals
        when(expenseSplitRepository.getTotalOwedByUser("user1")).thenReturn(new BigDecimal("33.33"));
        when(expenseSplitRepository.getTotalOwedToUser("user1")).thenReturn(new BigDecimal("66.67"));
        when(expenseRepository.findByUserIdOrderByDateDesc("user1")).thenReturn(Arrays.asList(expense));

        UserBalanceResponse response = expenseService.calculateUserBalance("user1");

        assertNotNull(response);
        assertEquals("user1", response.getUserId());
        // net = 66.67 - 33.33 = 33.34 > 0
        assertTrue(response.getNetBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateGroupBalances_ShouldReturnBalancesForAllUsers() {
        when(expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(1L))
                .thenReturn(Arrays.asList(expense));

        Map<String, BigDecimal> balances = expenseService.calculateGroupBalances(1L);

        assertNotNull(balances);
        // user1 paid 100, split 33.33 → net = +66.67
        assertTrue(balances.get("user1").compareTo(BigDecimal.ZERO) > 0);
        // user2 owes 33.33
        assertTrue(balances.get("user2").compareTo(BigDecimal.ZERO) < 0);
        // user3 owes 33.34
        assertTrue(balances.get("user3").compareTo(BigDecimal.ZERO) < 0);
    }
}
