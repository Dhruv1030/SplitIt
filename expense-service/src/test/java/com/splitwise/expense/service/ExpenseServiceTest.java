package com.splitwise.expense.service;

import com.splitwise.expense.client.ActivityClient;
import com.splitwise.expense.dto.*;
import com.splitwise.expense.exception.BadRequestException;
import com.splitwise.expense.exception.ResourceNotFoundException;
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
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ActivityClient activityClient;

    @InjectMocks
    private ExpenseService expenseService;

    private CreateExpenseRequest createExpenseRequest;
    private Expense expense;
    private List<ExpenseSplit> expenseSplits;

    @BeforeEach
    void setUp() {
        // Setup test data
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
                .splitType(SplitType.EQUAL)
                .date(LocalDateTime.now())
                .build();

        expenseSplits = Arrays.asList(
                ExpenseSplit.builder()
                        .id(1L)
                        .expense(expense)
                        .userId("user1")
                        .amount(new BigDecimal("33.33"))
                        .build(),
                ExpenseSplit.builder()
                        .id(2L)
                        .expense(expense)
                        .userId("user2")
                        .amount(new BigDecimal("33.33"))
                        .build(),
                ExpenseSplit.builder()
                        .id(3L)
                        .expense(expense)
                        .userId("user3")
                        .amount(new BigDecimal("33.34"))
                        .build());
    }

    @Test
    void createExpense_WithEqualSplit_ShouldSucceed() {
        // Arrange
        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseSplitRepository.saveAll(anyList())).thenReturn(expenseSplits);

        // Act
        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        // Assert
        assertNotNull(response);
        assertEquals("Dinner", response.getDescription());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(SplitType.EQUAL, response.getSplitType());
        verify(expenseRepository).save(any(Expense.class));
        verify(expenseSplitRepository).saveAll(anyList());
        verify(kafkaTemplate).send(eq("expense-events"), any());
    }

    @Test
    void createExpense_WithExactSplit_ShouldSucceed() {
        // Arrange
        createExpenseRequest.setSplitType(SplitType.EXACT);
        Map<String, BigDecimal> exactAmounts = new HashMap<>();
        exactAmounts.put("user1", new BigDecimal("40.00"));
        exactAmounts.put("user2", new BigDecimal("30.00"));
        exactAmounts.put("user3", new BigDecimal("30.00"));
        createExpenseRequest.setExactAmounts(exactAmounts);

        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseSplitRepository.saveAll(anyList())).thenReturn(expenseSplits);

        // Act
        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        // Assert
        assertNotNull(response);
        assertEquals(SplitType.EXACT, createExpenseRequest.getSplitType());
        verify(splitCalculatorService).calculateSplits(any(CreateExpenseRequest.class));
    }

    @Test
    void createExpense_WithPercentageSplit_ShouldSucceed() {
        // Arrange
        createExpenseRequest.setSplitType(SplitType.PERCENTAGE);
        Map<String, BigDecimal> percentages = new HashMap<>();
        percentages.put("user1", new BigDecimal("50"));
        percentages.put("user2", new BigDecimal("30"));
        percentages.put("user3", new BigDecimal("20"));
        createExpenseRequest.setPercentages(percentages);

        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseSplitRepository.saveAll(anyList())).thenReturn(expenseSplits);

        // Act
        ExpenseResponse response = expenseService.createExpense(createExpenseRequest, "user1");

        // Assert
        assertNotNull(response);
        assertEquals(SplitType.PERCENTAGE, createExpenseRequest.getSplitType());
        verify(splitCalculatorService).calculateSplits(any(CreateExpenseRequest.class));
    }

    @Test
    void getExpenseById_WhenExists_ShouldReturnExpense() {
        // Arrange
        expense.setSplits(expenseSplits);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // Act
        ExpenseResponse response = expenseService.getExpenseById(1L, "user1");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Dinner", response.getDescription());
        assertEquals(3, response.getSplits().size());
    }

    @Test
    void getExpenseById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(expenseRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpenseById(999L, "user1");
        });
    }

    @Test
    void getGroupExpenses_ShouldReturnAllExpenses() {
        // Arrange
        List<Expense> expenses = Arrays.asList(expense);
        when(expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(1L)).thenReturn(expenses);

        // Act
        List<ExpenseResponse> responses = expenseService.getGroupExpenses(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Dinner", responses.get(0).getDescription());
    }

    @Test
    void updateExpense_WhenAuthorized_ShouldSucceed() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(splitCalculatorService.calculateSplits(any(CreateExpenseRequest.class)))
                .thenReturn(expenseSplits);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(expenseSplitRepository.saveAll(anyList())).thenReturn(expenseSplits);

        CreateExpenseRequest updateRequest = new CreateExpenseRequest();
        updateRequest.setDescription("Updated Dinner");
        updateRequest.setAmount(new BigDecimal("120.00"));
        updateRequest.setPaidBy("user1");
        updateRequest.setSplitType(SplitType.EQUAL);
        updateRequest.setParticipantIds(Arrays.asList("user1", "user2"));

        // Act
        ExpenseResponse response = expenseService.updateExpense(1L, updateRequest, "user1");

        // Assert
        assertNotNull(response);
        verify(expenseSplitRepository).deleteAll(anyList());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void updateExpense_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        CreateExpenseRequest updateRequest = new CreateExpenseRequest();
        updateRequest.setDescription("Updated Dinner");

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            expenseService.updateExpense(1L, updateRequest, "user2");
        });
    }

    @Test
    void deleteExpense_WhenAuthorized_ShouldSucceed() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // Act
        expenseService.deleteExpense(1L, "user1");

        // Assert
        verify(expenseRepository).delete(expense);
        verify(kafkaTemplate).send(eq("expense-events"), any());
    }

    @Test
    void deleteExpense_WhenUnauthorized_ShouldThrowException() {
        // Arrange
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            expenseService.deleteExpense(1L, "user2");
        });
    }

    @Test
    void calculateUserBalance_ShouldReturnCorrectBalance() {
        // Arrange
        expense.setSplits(expenseSplits);
        List<Expense> userExpenses = Arrays.asList(expense);
        when(expenseRepository.findByUserIdOrderByDateDesc("user1"))
                .thenReturn(userExpenses);

        // Act
        UserBalanceResponse response = expenseService.calculateUserBalance("user1");

        // Assert
        assertNotNull(response);
        assertEquals("user1", response.getUserId());
        // user1 paid 100, owes 33.33, so netBalance = 100 - 33.33 = 66.67
        assertTrue(response.getNetBalance().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateGroupBalances_ShouldReturnBalancesForAllUsers() {
        // Arrange
        expense.setSplits(expenseSplits);
        when(expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(1L))
                .thenReturn(Arrays.asList(expense));

        // Act
        Map<String, BigDecimal> balances = expenseService.calculateGroupBalances(1L);

        // Assert
        assertNotNull(balances);
        assertTrue(balances.containsKey("user1"));
        assertTrue(balances.containsKey("user2"));
        assertTrue(balances.containsKey("user3"));
        // user1 should have positive balance (paid more than owed)
        assertTrue(balances.get("user1").compareTo(BigDecimal.ZERO) > 0);
        // user2 and user3 should have negative balance (owe money)
        assertTrue(balances.get("user2").compareTo(BigDecimal.ZERO) < 0);
        assertTrue(balances.get("user3").compareTo(BigDecimal.ZERO) < 0);
    }
}
