package com.splitwise.settlement.controller;

import com.splitwise.settlement.dto.*;
import com.splitwise.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
@Slf4j
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * Get simplified settlement suggestions for a group
     * This endpoint calculates the minimum number of transactions needed
     * 
     * GET /api/settlements/group/{groupId}/suggestions
     */
    @GetMapping("/group/{groupId}/suggestions")
    public ResponseEntity<SettlementSuggestionsResponse> getSettlementSuggestions(
            @PathVariable Long groupId) {
        log.info("Fetching settlement suggestions for group: {}", groupId);
        SettlementSuggestionsResponse response = settlementService.calculateSettlements(groupId);
        return ResponseEntity.ok(response);
    }

    /**
     * Record a settlement (when payment is made)
     * 
     * POST /api/settlements
     */
    @PostMapping
    public ResponseEntity<SettlementResponse> recordSettlement(
            @RequestBody RecordSettlementRequest request,
            @RequestHeader("X-User-Id") String currentUserId) {
        log.info("Recording settlement for user: {}", currentUserId);

        try {
            SettlementResponse response = settlementService.recordSettlement(request, currentUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid settlement request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all settlements for a group
     * 
     * GET /api/settlements/group/{groupId}
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<SettlementResponse>> getGroupSettlements(
            @PathVariable Long groupId) {
        log.info("Fetching settlements for group: {}", groupId);
        List<SettlementResponse> settlements = settlementService.getGroupSettlements(groupId);
        return ResponseEntity.ok(settlements);
    }

    /**
     * Get settlements for current user
     * 
     * GET /api/settlements/my-settlements
     */
    @GetMapping("/my-settlements")
    public ResponseEntity<List<SettlementResponse>> getMySettlements(
            @RequestHeader("X-User-Id") String currentUserId) {
        log.info("Fetching settlements for user: {}", currentUserId);
        List<SettlementResponse> settlements = settlementService.getUserSettlements(currentUserId);
        return ResponseEntity.ok(settlements);
    }

    /**
     * Mark a settlement as completed (for payee confirmation)
     * 
     * PUT /api/settlements/{id}/complete
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<SettlementResponse> completeSettlement(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String currentUserId) {
        log.info("Completing settlement {} by user: {}", id, currentUserId);

        try {
            SettlementResponse response = settlementService.completeSettlement(id, currentUserId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error completing settlement: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Settlement Service is running!");
    }
}
