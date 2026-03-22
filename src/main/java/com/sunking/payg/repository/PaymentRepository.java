package com.sunking.payg.repository;

import com.sunking.payg.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Paginated payment history for a customer — uses index on customer_id + created_at.
     */
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.createdAt DESC")
    Page<Payment> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") UUID customerId, Pageable pageable);

    /**
     * Paginated payment history for an assignment.
     */
    Page<Payment> findByAssignmentIdOrderByCreatedAtDesc(UUID assignmentId, Pageable pageable);

    Optional<Payment> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.assignment.id = :assignmentId AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByAssignment(@Param("assignmentId") UUID assignmentId);

    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.retryCount < :maxRetries AND p.createdAt > :since")
    Page<Payment> findFailedPaymentsForRetry(@Param("maxRetries") int maxRetries,
                                              @Param("since") LocalDateTime since,
                                              Pageable pageable);
}
