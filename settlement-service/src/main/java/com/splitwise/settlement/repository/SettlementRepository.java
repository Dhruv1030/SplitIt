package com.splitwise.settlement.repository;

import com.splitwise.settlement.entity.Settlement;
import com.splitwise.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * Find all settlements for a group
     */
    List<Settlement> findByGroupId(Long groupId);

    /**
     * Find settlements by status
     */
    List<Settlement> findByGroupIdAndStatus(Long groupId, SettlementStatus status);

    /**
     * Find settlements where user is involved (as payer or payee)
     */
    @Query("SELECT s FROM Settlement s WHERE s.groupId = :groupId AND (s.payerId = :userId OR s.payeeId = :userId)")
    List<Settlement> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") String userId);

    /**
     * Find settlements where user is payer
     */
    List<Settlement> findByPayerId(String payerId);

    /**
     * Find settlements where user is payee
     */
    List<Settlement> findByPayeeId(String payeeId);

    /**
     * Check if settlement exists between two users in a group
     */
    @Query("SELECT s FROM Settlement s WHERE s.groupId = :groupId AND s.payerId = :payerId AND s.payeeId = :payeeId AND s.status = :status")
    List<Settlement> findByGroupAndUsers(@Param("groupId") Long groupId,
            @Param("payerId") String payerId,
            @Param("payeeId") String payeeId,
            @Param("status") SettlementStatus status);
}
