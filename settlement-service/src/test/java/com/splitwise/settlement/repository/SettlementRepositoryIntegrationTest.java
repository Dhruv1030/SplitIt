package com.splitwise.settlement.repository;

import com.splitwise.settlement.entity.Settlement;
import com.splitwise.settlement.entity.SettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SettlementRepositoryIntegrationTest {

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private SettlementRepository settlementRepository;

    private Settlement savedSettlement;
    private final Long groupId = 1L;
    private final String payerId = "user-123";
    private final String payeeId = "user-456";

    @BeforeEach
    void setUp() {
        settlementRepository.deleteAll();

        Settlement settlement = Settlement.builder()
                .groupId(groupId)
                .payerId(payerId)
                .payeeId(payeeId)
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .status(SettlementStatus.PENDING)
                .build();

        savedSettlement = settlementRepository.save(settlement);
    }

    @Test
    void findByGroupId_returnsSettlements() {
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        assertEquals(1, settlements.size());
        assertEquals(payerId, settlements.get(0).getPayerId());
    }

    @Test
    void findByGroupIdAndStatus_returnsFilteredSettlements() {
        List<Settlement> pending = settlementRepository.findByGroupIdAndStatus(groupId, SettlementStatus.PENDING);
        assertEquals(1, pending.size());

        List<Settlement> completed = settlementRepository.findByGroupIdAndStatus(groupId, SettlementStatus.COMPLETED);
        assertTrue(completed.isEmpty());
    }

    @Test
    void findByGroupIdAndUserId_returnsSettlementsForUser() {
        List<Settlement> settlements = settlementRepository.findByGroupIdAndUserId(groupId, payerId);
        assertEquals(1, settlements.size());

        settlements = settlementRepository.findByGroupIdAndUserId(groupId, payeeId);
        assertEquals(1, settlements.size());

        settlements = settlementRepository.findByGroupIdAndUserId(groupId, "stranger");
        assertTrue(settlements.isEmpty());
    }

    @Test
    void findByPayerId_returnsSettlements() {
        List<Settlement> settlements = settlementRepository.findByPayerId(payerId);
        assertEquals(1, settlements.size());
    }

    @Test
    void findByPayeeId_returnsSettlements() {
        List<Settlement> settlements = settlementRepository.findByPayeeId(payeeId);
        assertEquals(1, settlements.size());
    }

    @Test
    void findByStatus_returnsSettlements() {
        List<Settlement> pending = settlementRepository.findByStatus(SettlementStatus.PENDING);
        assertEquals(1, pending.size());
    }

    @Test
    void updateSettlementStatus_completesSettlement() {
        savedSettlement.setStatus(SettlementStatus.COMPLETED);
        Settlement updated = settlementRepository.save(savedSettlement);

        assertEquals(SettlementStatus.COMPLETED, updated.getStatus());

        List<Settlement> pending = settlementRepository.findByStatus(SettlementStatus.PENDING);
        assertTrue(pending.isEmpty());
    }

    @Test
    void findByGroupAndUsers_returnsMatchingSettlements() {
        List<Settlement> settlements = settlementRepository.findByGroupAndUsers(
                groupId, payerId, payeeId, SettlementStatus.PENDING);
        assertEquals(1, settlements.size());
    }
}
