package com.splitwise.settlement.service;

import com.splitwise.settlement.dto.*;
import com.splitwise.settlement.entity.Settlement;
import com.splitwise.settlement.entity.SettlementStatus;
import com.splitwise.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final WebClient.Builder webClientBuilder;

    /**
     * Calculate simplified settlements for a group using debt simplification
     * algorithm
     * This minimizes the number of transactions needed to settle all debts
     */
    public SettlementSuggestionsResponse calculateSettlements(Long groupId) {
        log.info("Calculating settlements for group: {}", groupId);

        // Get balances from expense service
        Map<String, BigDecimal> balances = fetchGroupBalances(groupId);

        if (balances.isEmpty()) {
            return new SettlementSuggestionsResponse(groupId, Collections.emptyList());
        }

        // Apply debt simplification algorithm
        List<SettlementSuggestion> suggestions = simplifyDebts(balances);

        // Fetch user names for display
        enrichWithUserNames(suggestions);

        return new SettlementSuggestionsResponse(groupId, suggestions);
    }

    /**
     * Debt Simplification Algorithm (Greedy Approach)
     * 
     * Algorithm:
     * 1. Separate users into creditors (positive balance) and debtors (negative
     * balance)
     * 2. Sort creditors in descending order, debtors in ascending order
     * 3. Match the largest debtor with the largest creditor
     * 4. Create a transaction for the minimum of the two amounts
     * 5. Adjust balances and repeat until all debts are settled
     * 
     * Time Complexity: O(n log n) due to sorting
     * This approach minimizes the number of transactions to O(n-1) at most
     */
    private List<SettlementSuggestion> simplifyDebts(Map<String, BigDecimal> balances) {
        List<SettlementSuggestion> suggestions = new ArrayList<>();

        // Separate creditors (owed money) and debtors (owe money)
        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();

        for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new UserBalance(entry.getKey(), balance, null));
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new UserBalance(entry.getKey(), balance.abs(), null));
            }
        }

        // Sort creditors (descending) and debtors (descending)
        creditors.sort((a, b) -> b.getNetBalance().compareTo(a.getNetBalance()));
        debtors.sort((a, b) -> b.getNetBalance().compareTo(a.getNetBalance()));

        // Match creditors with debtors
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            UserBalance creditor = creditors.get(i);
            UserBalance debtor = debtors.get(j);

            // Amount to settle is minimum of what creditor is owed and what debtor owes
            BigDecimal settlementAmount = creditor.getNetBalance().min(debtor.getNetBalance());

            // Create settlement suggestion
            SettlementSuggestion suggestion = SettlementSuggestion.builder()
                    .payerId(debtor.getUserId())
                    .payeeId(creditor.getUserId())
                    .amount(settlementAmount.setScale(2, RoundingMode.HALF_UP))
                    .currency("USD")
                    .build();

            suggestions.add(suggestion);

            // Update balances
            creditor.setNetBalance(creditor.getNetBalance().subtract(settlementAmount));
            debtor.setNetBalance(debtor.getNetBalance().subtract(settlementAmount));

            // Move to next if balance is settled
            if (creditor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (debtor.getNetBalance().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        log.info("Generated {} settlement suggestions for group {}", suggestions.size(), balances.size());
        return suggestions;
    }

    /**
     * Fetch balances from Expense Service
     */
    private Map<String, BigDecimal> fetchGroupBalances(Long groupId) {
        try {
            // Call Expense Service to get balances
            String url = "http://expense-service/api/expenses/group/" + groupId + "/balances";

            @SuppressWarnings("unchecked")
            Map<String, Object> rawBalances = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (rawBalances == null || rawBalances.isEmpty()) {
                return Collections.emptyMap();
            }

            // Convert values to BigDecimal
            Map<String, BigDecimal> balances = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawBalances.entrySet()) {
                Object value = entry.getValue();
                BigDecimal balance;
                if (value instanceof Number) {
                    balance = new BigDecimal(value.toString());
                } else {
                    balance = new BigDecimal(String.valueOf(value));
                }
                balances.put(entry.getKey(), balance);
            }

            return balances;
        } catch (Exception e) {
            log.error("Error fetching balances from expense service: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Enrich suggestions with user names
     */
    private void enrichWithUserNames(List<SettlementSuggestion> suggestions) {
        // Extract unique user IDs
        Set<String> userIds = new HashSet<>();
        for (SettlementSuggestion suggestion : suggestions) {
            userIds.add(suggestion.getPayerId());
            userIds.add(suggestion.getPayeeId());
        }

        // Fetch user names from User Service
        Map<String, String> userNames = fetchUserNames(userIds);

        // Set names in suggestions
        for (SettlementSuggestion suggestion : suggestions) {
            suggestion.setPayerName(userNames.getOrDefault(suggestion.getPayerId(), "User"));
            suggestion.setPayeeName(userNames.getOrDefault(suggestion.getPayeeId(), "User"));
        }
    }

    /**
     * Fetch user names from User Service
     */
    private Map<String, String> fetchUserNames(Set<String> userIds) {
        Map<String, String> names = new HashMap<>();

        for (String userId : userIds) {
            try {
                String url = "http://user-service/api/users/" + userId;

                @SuppressWarnings("unchecked")
                Map<String, Object> user = webClientBuilder.build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (user != null && user.containsKey("name")) {
                    names.put(userId, (String) user.get("name"));
                } else {
                    names.put(userId, "User");
                }
            } catch (Exception e) {
                log.warn("Could not fetch name for user {}: {}", userId, e.getMessage());
                names.put(userId, "User");
            }
        }

        return names;
    }

    /**
     * Record a settlement (when users actually make payment)
     */
    @Transactional
    public SettlementResponse recordSettlement(RecordSettlementRequest request, String currentUserId) {
        log.info("Recording settlement: {} pays {} amount {}",
                request.getPayerId(), request.getPayeeId(), request.getAmount());

        // Validate that current user is the payer
        if (!currentUserId.equals(request.getPayerId())) {
            throw new IllegalArgumentException("You can only record settlements where you are the payer");
        }

        // Create settlement record
        Settlement settlement = Settlement.builder()
                .groupId(request.getGroupId())
                .payerId(request.getPayerId())
                .payeeId(request.getPayeeId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(SettlementStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .settledAt(LocalDateTime.now())
                .build();

        settlement = settlementRepository.save(settlement);

        log.info("Settlement recorded with ID: {}", settlement.getId());

        return toResponse(settlement);
    }

    /**
     * Get all settlements for a group
     */
    public List<SettlementResponse> getGroupSettlements(Long groupId) {
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        return settlements.stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Get settlements for current user
     */
    public List<SettlementResponse> getUserSettlements(String userId) {
        List<Settlement> paidSettlements = settlementRepository.findByPayerId(userId);
        List<Settlement> receivedSettlements = settlementRepository.findByPayeeId(userId);

        List<Settlement> allSettlements = new ArrayList<>();
        allSettlements.addAll(paidSettlements);
        allSettlements.addAll(receivedSettlements);

        return allSettlements.stream()
                .map(this::toResponse)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    /**
     * Mark a settlement as completed
     */
    @Transactional
    public SettlementResponse completeSettlement(Long settlementId, String currentUserId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found"));

        // Validate that current user is the payee
        if (!currentUserId.equals(settlement.getPayeeId())) {
            throw new IllegalArgumentException("Only the payee can confirm settlement completion");
        }

        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement.setSettledAt(LocalDateTime.now());

        settlement = settlementRepository.save(settlement);

        return toResponse(settlement);
    }

    /**
     * Convert entity to response DTO
     */
    private SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .groupId(settlement.getGroupId())
                .payerId(settlement.getPayerId())
                .payeeId(settlement.getPayeeId())
                .amount(settlement.getAmount())
                .currency(settlement.getCurrency())
                .status(settlement.getStatus())
                .paymentMethod(settlement.getPaymentMethod())
                .transactionId(settlement.getTransactionId())
                .notes(settlement.getNotes())
                .settledAt(settlement.getSettledAt())
                .createdAt(settlement.getCreatedAt())
                .build();
    }
}
