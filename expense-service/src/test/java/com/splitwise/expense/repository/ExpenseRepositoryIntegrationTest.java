package com.splitwise.expense.repository;

import com.splitwise.expense.model.Expense;
import com.splitwise.expense.model.ExpenseSplit;
import com.splitwise.expense.model.SplitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryIntegrationTest {

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseSplitRepository expenseSplitRepository;

    private Expense savedExpense;
    private final String payerId = "user-123";
    private final String splitUserId = "user-456";
    private final Long groupId = 1L;

    @BeforeEach
    void setUp() {
        expenseSplitRepository.deleteAll();
        expenseRepository.deleteAll();

        Expense expense = Expense.builder()
                .description("Dinner")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .groupId(groupId)
                .paidBy(payerId)
                .createdBy(payerId)
                .category("FOOD")
                .splitType(SplitType.EQUAL)
                .date(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .splits(new ArrayList<>())
                .build();

        savedExpense = expenseRepository.save(expense);

        ExpenseSplit split1 = ExpenseSplit.builder()
                .userId(payerId)
                .amount(new BigDecimal("50.00"))
                .isPaid(true)
                .expense(savedExpense)
                .build();

        ExpenseSplit split2 = ExpenseSplit.builder()
                .userId(splitUserId)
                .amount(new BigDecimal("50.00"))
                .isPaid(false)
                .expense(savedExpense)
                .build();

        expenseSplitRepository.save(split1);
        expenseSplitRepository.save(split2);
    }

    @Test
    void findByIdAndIsActiveTrue_returnsExpense() {
        Optional<Expense> found = expenseRepository.findByIdAndIsActiveTrue(savedExpense.getId());
        assertTrue(found.isPresent());
        assertEquals("Dinner", found.get().getDescription());
    }

    @Test
    void findByIdAndIsActiveTrue_returnsEmptyForInactive() {
        savedExpense.setIsActive(false);
        expenseRepository.save(savedExpense);

        Optional<Expense> found = expenseRepository.findByIdAndIsActiveTrue(savedExpense.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    void findByGroupIdAndIsActiveTrueOrderByDateDesc_returnsExpenses() {
        List<Expense> expenses = expenseRepository.findByGroupIdAndIsActiveTrueOrderByDateDesc(groupId);
        assertEquals(1, expenses.size());
        assertEquals("Dinner", expenses.get(0).getDescription());
    }

    @Test
    void findByUserIdOrderByDateDesc_returnsExpensesForUser() {
        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateDesc(splitUserId);
        assertEquals(1, expenses.size());
    }

    @Test
    void countByGroupId_returnsCorrectCount() {
        int count = expenseRepository.countByGroupId(groupId);
        assertEquals(1, count);
    }

    @Test
    void getTotalOwedByUser_returnsCorrectAmount() {
        BigDecimal owed = expenseSplitRepository.getTotalOwedByUser(splitUserId);
        assertNotNull(owed);
        assertEquals(0, new BigDecimal("50.00").compareTo(owed));
    }

    @Test
    void getTotalOwedToUser_returnsCorrectAmount() {
        BigDecimal owedTo = expenseSplitRepository.getTotalOwedToUser(payerId);
        assertNotNull(owedTo);
        assertEquals(0, new BigDecimal("50.00").compareTo(owedTo));
    }

    @Test
    void findByExpenseId_returnsSplits() {
        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(savedExpense.getId());
        assertEquals(2, splits.size());
    }
}
