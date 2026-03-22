package com.sunking.payg.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "device_assignments",
    indexes = {
        @Index(name = "idx_assignment_customer", columnList = "customer_id"),
        @Index(name = "idx_assignment_device", columnList = "device_id"),
        @Index(name = "idx_assignment_active", columnList = "is_active"),
        @Index(name = "idx_assignment_next_payment", columnList = "next_payment_due_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "unassigned_at")
    private LocalDateTime unassignedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "total_cost_at_assignment", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCostAtAssignment;

    @Column(name = "daily_rate_at_assignment", nullable = false, precision = 15, scale = 2)
    private BigDecimal dailyRateAtAssignment;

    @Column(name = "amount_paid", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "next_payment_due_date")
    private LocalDate nextPaymentDueDate;

    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    @Column(name = "days_overdue")
    @Builder.Default
    private Integer daysOverdue = 0;

    @Column(name = "is_fully_paid", nullable = false)
    @Builder.Default
    private Boolean isFullyPaid = false;

    @Column(name = "fully_paid_at")
    private LocalDateTime fullyPaidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public BigDecimal getRemainingBalance() {
        return totalCostAtAssignment.subtract(amountPaid);
    }
}
